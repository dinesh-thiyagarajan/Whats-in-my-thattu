#!/usr/bin/env python3
"""
Evaluation utilities for the food recognition model.

Provides detailed analysis including:
  - Per-class accuracy breakdown
  - Confusion matrix visualization
  - Top-N accuracy metrics
  - Misclassification analysis
  - Comparison between old and new models

Usage:
    python evaluate.py
    python evaluate.py --checkpoint checkpoints/final_model.keras
    python evaluate.py --tflite exported_model/whats_in_my_thattu_v2.tflite
"""

import argparse
import os
import json

import numpy as np
import tensorflow as tf
from tqdm import tqdm

from config import (
    BATCH_SIZE,
    CHECKPOINT_DIR,
    EXPORT_DIR,
    IMAGE_SIZE,
    LABEL_MAP_PATH,
    NUM_CLASSES,
    TFLITE_MODEL_PATH,
)
from dataset import load_datasets


def evaluate_keras_model(model, test_ds, class_names):
    """
    Evaluate a Keras model and produce detailed metrics.

    Args:
        model: Trained Keras model.
        test_ds: Test dataset (batched, preprocessed).
        class_names: List of class name strings.

    Returns:
        Dictionary with evaluation results.
    """
    print("Evaluating Keras model on test set...")

    # Overall metrics
    results = model.evaluate(test_ds, verbose=1)
    metrics = dict(zip(model.metrics_names, results))

    # Per-class predictions
    all_preds = []
    all_labels = []

    for images, labels in tqdm(test_ds, desc="Predicting"):
        predictions = model.predict(images, verbose=0)
        all_preds.append(predictions)
        all_labels.append(labels.numpy())

    all_preds = np.concatenate(all_preds, axis=0)
    all_labels = np.concatenate(all_labels, axis=0)

    pred_classes = np.argmax(all_preds, axis=1)
    true_classes = np.argmax(all_labels, axis=1)

    return _compute_detailed_metrics(
        pred_classes, true_classes, all_preds, class_names, metrics
    )


def evaluate_tflite_model(tflite_path, test_ds, class_names):
    """
    Evaluate a TFLite model and produce detailed metrics.

    Args:
        tflite_path: Path to .tflite model file.
        test_ds: Test dataset (batched, preprocessed).
        class_names: List of class name strings.

    Returns:
        Dictionary with evaluation results.
    """
    print(f"Evaluating TFLite model: {tflite_path}")

    interpreter = tf.lite.Interpreter(model_path=tflite_path)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    all_preds = []
    all_labels = []

    for images, labels in tqdm(test_ds, desc="TFLite inference"):
        batch_preds = []
        for i in range(images.shape[0]):
            input_data = np.expand_dims(images[i].numpy(), axis=0)

            if input_details[0]["dtype"] == np.uint8:
                input_data = (input_data * 255).astype(np.uint8)
            else:
                input_data = input_data.astype(np.float32)

            interpreter.set_tensor(input_details[0]["index"], input_data)
            interpreter.invoke()
            output = interpreter.get_tensor(output_details[0]["index"])
            batch_preds.append(output[0])

        all_preds.append(np.array(batch_preds))
        all_labels.append(labels.numpy())

    all_preds = np.concatenate(all_preds, axis=0)
    all_labels = np.concatenate(all_labels, axis=0)

    pred_classes = np.argmax(all_preds, axis=1)
    true_classes = np.argmax(all_labels, axis=1)

    # Compute basic metrics
    accuracy = np.mean(pred_classes == true_classes)
    metrics = {"accuracy": accuracy}

    return _compute_detailed_metrics(
        pred_classes, true_classes, all_preds, class_names, metrics
    )


def _compute_detailed_metrics(pred_classes, true_classes, all_preds, class_names, metrics):
    """Compute per-class and aggregate metrics."""
    num_samples = len(true_classes)

    # Top-1 accuracy
    top1_acc = np.mean(pred_classes == true_classes)

    # Top-5 accuracy
    top5_preds = np.argsort(all_preds, axis=1)[:, -5:]
    top5_correct = sum(
        true_classes[i] in top5_preds[i] for i in range(num_samples)
    )
    top5_acc = top5_correct / num_samples

    # Per-class accuracy
    per_class_metrics = []
    for i, name in enumerate(class_names):
        mask = true_classes == i
        if mask.sum() == 0:
            continue
        class_acc = np.mean(pred_classes[mask] == i)
        class_count = mask.sum()
        per_class_metrics.append({
            "class_id": i,
            "name": name.replace("_", " ").title(),
            "accuracy": float(class_acc),
            "sample_count": int(class_count),
        })

    per_class_metrics.sort(key=lambda x: x["accuracy"])

    # Confusion pairs (most confused classes)
    from collections import Counter
    confusion_pairs = Counter()
    for true_cls, pred_cls in zip(true_classes, pred_classes):
        if true_cls != pred_cls:
            pair = (class_names[true_cls], class_names[pred_cls])
            confusion_pairs[pair] += 1

    top_confusions = [
        {
            "true_class": pair[0].replace("_", " ").title(),
            "predicted_as": pair[1].replace("_", " ").title(),
            "count": count,
        }
        for pair, count in confusion_pairs.most_common(20)
    ]

    results = {
        "overall": {
            **metrics,
            "top1_accuracy": float(top1_acc),
            "top5_accuracy": float(top5_acc),
            "total_samples": num_samples,
        },
        "per_class": per_class_metrics,
        "top_confusions": top_confusions,
        "worst_classes": per_class_metrics[:10],
        "best_classes": per_class_metrics[-10:],
    }

    # Print summary
    print(f"\n{'='*60}")
    print("EVALUATION RESULTS")
    print(f"{'='*60}")
    print(f"  Total samples:     {num_samples}")
    print(f"  Top-1 Accuracy:    {top1_acc*100:.2f}%")
    print(f"  Top-5 Accuracy:    {top5_acc*100:.2f}%")

    print(f"\n  Best performing classes:")
    for cls in per_class_metrics[-5:]:
        print(f"    {cls['name']:30s} {cls['accuracy']*100:.1f}% ({cls['sample_count']} samples)")

    print(f"\n  Worst performing classes:")
    for cls in per_class_metrics[:5]:
        print(f"    {cls['name']:30s} {cls['accuracy']*100:.1f}% ({cls['sample_count']} samples)")

    print(f"\n  Most confused pairs:")
    for conf in top_confusions[:5]:
        print(f"    {conf['true_class']:25s} -> {conf['predicted_as']:25s} ({conf['count']} times)")

    return results


def save_evaluation_report(results, output_path):
    """Save evaluation results to a JSON file."""
    with open(output_path, "w") as f:
        json.dump(results, f, indent=2)
    print(f"\nEvaluation report saved to {output_path}")


def main():
    parser = argparse.ArgumentParser(
        description="Evaluate the food recognition model"
    )
    parser.add_argument(
        "--checkpoint",
        type=str,
        default=os.path.join(CHECKPOINT_DIR, "final_model.keras"),
        help="Path to trained .keras model",
    )
    parser.add_argument(
        "--tflite",
        type=str,
        default=None,
        help="Path to .tflite model (evaluates TFLite instead of Keras)",
    )
    parser.add_argument(
        "--output",
        type=str,
        default=os.path.join(EXPORT_DIR, "evaluation_report.json"),
        help="Output path for evaluation report",
    )
    args = parser.parse_args()

    # Load dataset
    print("Loading test dataset...")
    _, _, test_ds, class_names = load_datasets()

    # Evaluate
    if args.tflite:
        results = evaluate_tflite_model(args.tflite, test_ds, class_names)
    else:
        print(f"Loading model from {args.checkpoint}...")
        model = tf.keras.models.load_model(args.checkpoint)
        results = evaluate_keras_model(model, test_ds, class_names)

    # Save report
    os.makedirs(os.path.dirname(args.output), exist_ok=True)
    save_evaluation_report(results, args.output)


if __name__ == "__main__":
    main()
