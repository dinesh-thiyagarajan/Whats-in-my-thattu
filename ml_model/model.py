"""
Model architecture for food recognition using transfer learning.

Uses EfficientNetV2B0 as the base model — chosen for its excellent
accuracy-to-size ratio, making it ideal for mobile deployment.
Compared to the original AIY Vision model, EfficientNetV2B0 provides:
  - Higher top-1 accuracy on ImageNet (78.7% vs ~70%)
  - Better feature extraction for fine-grained food classification
  - Efficient inference suitable for on-device use (~7MB base)
"""

import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers, regularizers

from config import (
    CHANNELS,
    DROPOUT_RATE,
    FINE_TUNE_AT_LAYER,
    IMAGE_SIZE,
    NUM_CLASSES,
    WEIGHT_DECAY,
)


def build_model(num_classes=NUM_CLASSES, fine_tune=False):
    """
    Build a food classification model using EfficientNetV2B0 transfer learning.

    Architecture:
        Input (224x224x3)
        -> EfficientNetV2B0 (pretrained on ImageNet, frozen initially)
        -> GlobalAveragePooling2D
        -> BatchNormalization
        -> Dense(512, relu, L2 regularization)
        -> Dropout(0.3)
        -> BatchNormalization
        -> Dense(256, relu, L2 regularization)
        -> Dropout(0.3)
        -> Dense(num_classes, softmax)

    Args:
        num_classes: Number of food categories to classify.
        fine_tune: If True, unfreeze top layers of base model.

    Returns:
        Compiled Keras model.
    """
    # Input layer
    inputs = keras.Input(shape=(IMAGE_SIZE, IMAGE_SIZE, CHANNELS))

    # Base model — EfficientNetV2B0 pretrained on ImageNet
    base_model = keras.applications.EfficientNetV2B0(
        include_top=False,
        weights="imagenet",
        input_tensor=inputs,
        include_preprocessing=False,  # We handle preprocessing in dataset.py
    )

    # Freeze/unfreeze base model layers
    if fine_tune:
        base_model.trainable = True
        for layer in base_model.layers[:FINE_TUNE_AT_LAYER]:
            layer.trainable = False
        print(
            f"Fine-tuning: unfreezing layers from index {FINE_TUNE_AT_LAYER} "
            f"({len(base_model.layers) - FINE_TUNE_AT_LAYER} trainable layers)"
        )
    else:
        base_model.trainable = False
        print("Transfer learning: base model frozen")

    # Classification head
    x = base_model.output
    x = layers.GlobalAveragePooling2D(name="global_avg_pool")(x)
    x = layers.BatchNormalization(name="bn_1")(x)

    x = layers.Dense(
        512,
        activation="relu",
        kernel_regularizer=regularizers.l2(WEIGHT_DECAY),
        name="dense_1",
    )(x)
    x = layers.Dropout(DROPOUT_RATE, name="dropout_1")(x)
    x = layers.BatchNormalization(name="bn_2")(x)

    x = layers.Dense(
        256,
        activation="relu",
        kernel_regularizer=regularizers.l2(WEIGHT_DECAY),
        name="dense_2",
    )(x)
    x = layers.Dropout(DROPOUT_RATE, name="dropout_2")(x)

    # Output layer with softmax for multi-class classification
    outputs = layers.Dense(
        num_classes,
        activation="softmax",
        name="predictions",
    )(x)

    model = keras.Model(inputs=inputs, outputs=outputs, name="food_classifier_v2")

    # Print model summary
    trainable_count = sum(
        tf.keras.backend.count_params(w) for w in model.trainable_weights
    )
    non_trainable_count = sum(
        tf.keras.backend.count_params(w) for w in model.non_trainable_weights
    )
    print(f"\nModel: {model.name}")
    print(f"  Total params: {trainable_count + non_trainable_count:,}")
    print(f"  Trainable params: {trainable_count:,}")
    print(f"  Non-trainable params: {non_trainable_count:,}")
    print(f"  Output classes: {num_classes}")

    return model


def unfreeze_for_fine_tuning(model):
    """
    Unfreeze the top layers of the base model for fine-tuning.

    This is called after the initial training phase to allow
    the pre-trained weights to adapt to food-specific features.

    Args:
        model: The compiled Keras model with a frozen base.

    Returns:
        The model with top base layers unfrozen.
    """
    base_model = model.layers[1] if hasattr(model.layers[1], "layers") else None

    if base_model is None:
        # Find the EfficientNet base within the model
        for layer in model.layers:
            if "efficientnet" in layer.name.lower():
                base_model = layer
                break

    if base_model is None:
        print("Warning: Could not find base model for fine-tuning")
        return model

    base_model.trainable = True

    # Freeze early layers, unfreeze from FINE_TUNE_AT_LAYER onwards
    for layer in base_model.layers[:FINE_TUNE_AT_LAYER]:
        layer.trainable = False

    unfrozen = sum(1 for l in base_model.layers if l.trainable)
    frozen = sum(1 for l in base_model.layers if not l.trainable)
    print(f"\nFine-tuning setup:")
    print(f"  Frozen layers: {frozen}")
    print(f"  Unfrozen layers: {unfrozen}")

    return model


def get_model_info(model):
    """Return a dictionary of model metadata."""
    return {
        "name": model.name,
        "input_shape": model.input_shape,
        "output_shape": model.output_shape,
        "total_params": model.count_params(),
        "trainable_params": sum(
            tf.keras.backend.count_params(w) for w in model.trainable_weights
        ),
        "layers": len(model.layers),
    }
