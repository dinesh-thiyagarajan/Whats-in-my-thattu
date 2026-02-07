#!/usr/bin/env python3
"""
Training script for the What's In My Thattu food recognition model.

Training follows a two-phase approach:
  Phase 1 — Transfer Learning:
    Train only the classification head with the base model frozen.
    Uses a higher learning rate to quickly learn food-specific features.

  Phase 2 — Fine-Tuning:
    Unfreeze the top layers of the base model and train end-to-end
    with a much lower learning rate to refine pre-trained features
    for food classification.

Usage:
    python train.py
    python train.py --epochs 50 --batch-size 64
    python train.py --fine-tune-only --checkpoint checkpoints/best_model.keras
"""

import argparse
import os
import sys
import json
from datetime import datetime

import tensorflow as tf
from tensorflow import keras

from config import (
    BATCH_SIZE,
    CHECKPOINT_DIR,
    EARLY_STOPPING_PATIENCE,
    EPOCHS,
    EXPORT_DIR,
    FINE_TUNE_EPOCHS,
    FINE_TUNE_LEARNING_RATE,
    INITIAL_LEARNING_RATE,
    LABEL_SMOOTHING,
    MIN_LEARNING_RATE,
    NUM_CLASSES,
    REDUCE_LR_FACTOR,
    REDUCE_LR_PATIENCE,
)
from dataset import load_datasets
from model import build_model, unfreeze_for_fine_tuning


def create_callbacks(phase_name):
    """Create training callbacks for monitoring and checkpointing."""
    os.makedirs(CHECKPOINT_DIR, exist_ok=True)

    callbacks = [
        # Save best model based on validation accuracy
        keras.callbacks.ModelCheckpoint(
            filepath=os.path.join(CHECKPOINT_DIR, f"best_{phase_name}.keras"),
            monitor="val_accuracy",
            mode="max",
            save_best_only=True,
            verbose=1,
        ),
        # Early stopping to prevent overfitting
        keras.callbacks.EarlyStopping(
            monitor="val_accuracy",
            patience=EARLY_STOPPING_PATIENCE,
            mode="max",
            restore_best_weights=True,
            verbose=1,
        ),
        # Reduce learning rate on plateau
        keras.callbacks.ReduceLROnPlateau(
            monitor="val_loss",
            factor=REDUCE_LR_FACTOR,
            patience=REDUCE_LR_PATIENCE,
            min_lr=MIN_LEARNING_RATE,
            verbose=1,
        ),
        # TensorBoard logging
        keras.callbacks.TensorBoard(
            log_dir=os.path.join(CHECKPOINT_DIR, "logs", phase_name),
            histogram_freq=1,
        ),
    ]

    return callbacks


def compile_model(model, learning_rate):
    """Compile model with optimizer, loss, and metrics."""
    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=learning_rate),
        loss=keras.losses.CategoricalCrossentropy(
            label_smoothing=LABEL_SMOOTHING
        ),
        metrics=[
            "accuracy",
            keras.metrics.TopKCategoricalAccuracy(k=5, name="top5_accuracy"),
        ],
    )
    return model


def train_phase1(model, train_ds, val_ds, epochs):
    """
    Phase 1: Transfer Learning — train classification head only.

    The base EfficientNetV2B0 model is frozen and only the custom
    classification layers are trained.
    """
    print("\n" + "=" * 60)
    print("PHASE 1: Transfer Learning (frozen base)")
    print("=" * 60)

    model = compile_model(model, INITIAL_LEARNING_RATE)
    callbacks = create_callbacks("phase1_transfer")

    history = model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=epochs,
        callbacks=callbacks,
    )

    return model, history


def train_phase2(model, train_ds, val_ds, epochs):
    """
    Phase 2: Fine-Tuning — unfreeze top base model layers.

    Uses a much lower learning rate to carefully adjust the
    pre-trained weights for food-specific features.
    """
    print("\n" + "=" * 60)
    print("PHASE 2: Fine-Tuning (unfreezing top layers)")
    print("=" * 60)

    model = unfreeze_for_fine_tuning(model)
    model = compile_model(model, FINE_TUNE_LEARNING_RATE)
    callbacks = create_callbacks("phase2_finetune")

    history = model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=epochs,
        callbacks=callbacks,
    )

    return model, history


def save_training_report(history_p1, history_p2, test_results):
    """Save training metrics to a JSON report."""
    os.makedirs(EXPORT_DIR, exist_ok=True)

    report = {
        "timestamp": datetime.now().isoformat(),
        "phase1": {
            "epochs_completed": len(history_p1.history["accuracy"]),
            "final_train_accuracy": float(history_p1.history["accuracy"][-1]),
            "final_val_accuracy": float(history_p1.history["val_accuracy"][-1]),
            "best_val_accuracy": float(max(history_p1.history["val_accuracy"])),
        },
        "phase2": {
            "epochs_completed": len(history_p2.history["accuracy"]),
            "final_train_accuracy": float(history_p2.history["accuracy"][-1]),
            "final_val_accuracy": float(history_p2.history["val_accuracy"][-1]),
            "best_val_accuracy": float(max(history_p2.history["val_accuracy"])),
        },
        "test_evaluation": {
            "loss": float(test_results[0]),
            "accuracy": float(test_results[1]),
            "top5_accuracy": float(test_results[2]),
        },
    }

    report_path = os.path.join(EXPORT_DIR, "training_report.json")
    with open(report_path, "w") as f:
        json.dump(report, f, indent=2)
    print(f"\nTraining report saved to {report_path}")
    return report


def main():
    parser = argparse.ArgumentParser(
        description="Train the What's In My Thattu food recognition model"
    )
    parser.add_argument(
        "--epochs", type=int, default=EPOCHS,
        help=f"Number of epochs for phase 1 (default: {EPOCHS})"
    )
    parser.add_argument(
        "--fine-tune-epochs", type=int, default=FINE_TUNE_EPOCHS,
        help=f"Number of epochs for phase 2 (default: {FINE_TUNE_EPOCHS})"
    )
    parser.add_argument(
        "--batch-size", type=int, default=BATCH_SIZE,
        help=f"Batch size (default: {BATCH_SIZE})"
    )
    parser.add_argument(
        "--fine-tune-only", action="store_true",
        help="Skip phase 1 and only run fine-tuning"
    )
    parser.add_argument(
        "--checkpoint", type=str, default=None,
        help="Path to checkpoint to resume from"
    )
    parser.add_argument(
        "--gpu", type=int, default=None,
        help="GPU device index to use (default: auto)"
    )
    args = parser.parse_args()

    # GPU configuration
    if args.gpu is not None:
        gpus = tf.config.list_physical_devices("GPU")
        if gpus:
            tf.config.set_visible_devices(gpus[args.gpu], "GPU")
            tf.config.experimental.set_memory_growth(gpus[args.gpu], True)
            print(f"Using GPU: {gpus[args.gpu]}")
    else:
        gpus = tf.config.list_physical_devices("GPU")
        for gpu in gpus:
            tf.config.experimental.set_memory_growth(gpu, True)
        print(f"Available GPUs: {len(gpus)}")

    print(f"TensorFlow version: {tf.__version__}")
    print(f"Configuration: epochs={args.epochs}, fine_tune_epochs={args.fine_tune_epochs}, "
          f"batch_size={args.batch_size}")

    # Load dataset
    print("\nLoading dataset...")
    train_ds, val_ds, test_ds, class_names = load_datasets()

    # Build or load model
    if args.checkpoint:
        print(f"\nLoading model from checkpoint: {args.checkpoint}")
        model = keras.models.load_model(args.checkpoint)
    else:
        print("\nBuilding model...")
        model = build_model(num_classes=NUM_CLASSES, fine_tune=False)

    # Phase 1: Transfer Learning
    if not args.fine_tune_only:
        model, history_p1 = train_phase1(model, train_ds, val_ds, args.epochs)
    else:
        history_p1 = type("History", (), {"history": {"accuracy": [0], "val_accuracy": [0]}})()

    # Phase 2: Fine-Tuning
    model, history_p2 = train_phase2(model, train_ds, val_ds, args.fine_tune_epochs)

    # Evaluate on test set
    print("\n" + "=" * 60)
    print("EVALUATION ON TEST SET")
    print("=" * 60)
    test_results = model.evaluate(test_ds)
    print(f"  Test Loss:          {test_results[0]:.4f}")
    print(f"  Test Accuracy:      {test_results[1]:.4f} ({test_results[1]*100:.1f}%)")
    print(f"  Test Top-5 Accuracy: {test_results[2]:.4f} ({test_results[2]*100:.1f}%)")

    # Save final model
    final_model_path = os.path.join(CHECKPOINT_DIR, "final_model.keras")
    model.save(final_model_path)
    print(f"\nFinal model saved to {final_model_path}")

    # Save training report
    report = save_training_report(history_p1, history_p2, test_results)

    print("\n" + "=" * 60)
    print("TRAINING COMPLETE")
    print("=" * 60)
    print(f"  Phase 1 best val accuracy: {report['phase1']['best_val_accuracy']*100:.1f}%")
    print(f"  Phase 2 best val accuracy: {report['phase2']['best_val_accuracy']*100:.1f}%")
    print(f"  Test accuracy:             {report['test_evaluation']['accuracy']*100:.1f}%")
    print(f"  Test top-5 accuracy:       {report['test_evaluation']['top5_accuracy']*100:.1f}%")
    print(f"\nNext step: Run 'python export_tflite.py' to export for Android")


if __name__ == "__main__":
    main()
