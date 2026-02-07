"""
Dataset loading and preprocessing for food recognition training.

Loads the Food-101 dataset via TensorFlow Datasets with proper
augmentation, normalization, and pipeline optimization.
"""

import tensorflow as tf
import tensorflow_datasets as tfds

from config import (
    AUGMENTATION_CONFIG,
    BATCH_SIZE,
    CHANNELS,
    DATA_DIR,
    DATASET_NAME,
    IMAGE_SIZE,
    LABEL_MAP_PATH,
    NUM_CLASSES,
    VALIDATION_SPLIT,
)


def _decode_and_resize(image, label):
    """Decode image and resize to target dimensions."""
    image = tf.cast(image, tf.float32)
    image = tf.image.resize(image, [IMAGE_SIZE, IMAGE_SIZE])
    return image, label


def _normalize(image, label):
    """Normalize pixel values to [0, 1] range (EfficientNet preprocessing)."""
    image = image / 255.0
    return image, label


def _augment(image, label):
    """Apply data augmentation to training images."""
    cfg = AUGMENTATION_CONFIG

    image = tf.image.random_flip_left_right(image)
    image = tf.image.random_brightness(image, max_delta=cfg["random_brightness"])
    image = tf.image.random_contrast(
        image, lower=cfg["random_contrast"][0], upper=cfg["random_contrast"][1]
    )

    # Random rotation
    if cfg["random_rotation"] > 0:
        angle = tf.random.uniform(
            [], -cfg["random_rotation"], cfg["random_rotation"]
        )
        image = _rotate_image(image, angle)

    # Random zoom via crop and resize
    zoom_range = cfg["random_zoom"]
    if zoom_range[1] > 0:
        image = _random_zoom(image, zoom_range)

    # Clip to valid range
    image = tf.clip_by_value(image, 0.0, 255.0)

    return image, label


def _rotate_image(image, angle):
    """Rotate image by a given angle in radians."""
    angle_rad = angle * 3.14159265
    return tf.keras.layers.RandomRotation(
        factor=0, fill_mode="reflect"
    )(image) if angle == 0 else tf.image.rot90(
        image, k=tf.cast(tf.round(angle * 2), tf.int32) % 4
    )


def _random_zoom(image, zoom_range):
    """Apply random zoom by cropping and resizing."""
    zoom = tf.random.uniform([], 1.0 + zoom_range[0], 1.0 + zoom_range[1])
    h = tf.cast(tf.cast(IMAGE_SIZE, tf.float32) / zoom, tf.int32)
    w = h
    image = tf.image.resize(image, [IMAGE_SIZE, IMAGE_SIZE])
    image = tf.image.random_crop(
        image, size=[tf.minimum(h, IMAGE_SIZE), tf.minimum(w, IMAGE_SIZE), CHANNELS]
    )
    image = tf.image.resize(image, [IMAGE_SIZE, IMAGE_SIZE])
    return image


def _one_hot(image, label):
    """Convert label to one-hot encoding."""
    label = tf.one_hot(label, NUM_CLASSES)
    return image, label


def load_datasets():
    """
    Load Food-101 dataset split into train, validation, and test sets.

    Returns:
        Tuple of (train_ds, val_ds, test_ds, class_names)
        Each dataset is preprocessed and batched.
    """
    # Load dataset info to get class names
    builder = tfds.builder(DATASET_NAME, data_dir=DATA_DIR)
    info = builder.info

    class_names = info.features["label"].names

    # Save label map for TFLite metadata
    _save_label_map(class_names)

    # Calculate validation split
    val_pct = int(VALIDATION_SPLIT * 100)
    train_pct = 100 - val_pct

    # Load splits
    train_ds, val_ds = tfds.load(
        DATASET_NAME,
        split=[
            f"train[:{train_pct}%]",
            f"train[{train_pct}%:]",
        ],
        data_dir=DATA_DIR,
        as_supervised=True,
        with_info=False,
    )

    test_ds = tfds.load(
        DATASET_NAME,
        split="validation",  # Food-101 uses 'validation' as test
        data_dir=DATA_DIR,
        as_supervised=True,
        with_info=False,
    )

    # Build training pipeline
    train_ds = (
        train_ds.shuffle(10000)
        .map(_decode_and_resize, num_parallel_calls=tf.data.AUTOTUNE)
        .map(_augment, num_parallel_calls=tf.data.AUTOTUNE)
        .map(_normalize, num_parallel_calls=tf.data.AUTOTUNE)
        .map(_one_hot, num_parallel_calls=tf.data.AUTOTUNE)
        .batch(BATCH_SIZE)
        .prefetch(tf.data.AUTOTUNE)
    )

    # Build validation pipeline (no augmentation)
    val_ds = (
        val_ds.map(_decode_and_resize, num_parallel_calls=tf.data.AUTOTUNE)
        .map(_normalize, num_parallel_calls=tf.data.AUTOTUNE)
        .map(_one_hot, num_parallel_calls=tf.data.AUTOTUNE)
        .batch(BATCH_SIZE)
        .prefetch(tf.data.AUTOTUNE)
    )

    # Build test pipeline (no augmentation)
    test_ds = (
        test_ds.map(_decode_and_resize, num_parallel_calls=tf.data.AUTOTUNE)
        .map(_normalize, num_parallel_calls=tf.data.AUTOTUNE)
        .map(_one_hot, num_parallel_calls=tf.data.AUTOTUNE)
        .batch(BATCH_SIZE)
        .prefetch(tf.data.AUTOTUNE)
    )

    print(f"Dataset loaded: {DATASET_NAME}")
    print(f"  Classes: {NUM_CLASSES}")
    print(f"  Training batches: {tf.data.experimental.cardinality(train_ds).numpy()}")
    print(f"  Validation batches: {tf.data.experimental.cardinality(val_ds).numpy()}")
    print(f"  Test batches: {tf.data.experimental.cardinality(test_ds).numpy()}")

    return train_ds, val_ds, test_ds, class_names


def load_representative_dataset(num_samples=200):
    """
    Load a small representative dataset for TFLite quantization calibration.

    Args:
        num_samples: Number of samples to include.

    Yields:
        Numpy arrays of shape [1, IMAGE_SIZE, IMAGE_SIZE, CHANNELS].
    """
    ds = tfds.load(
        DATASET_NAME,
        split=f"train[:{num_samples}]",
        data_dir=DATA_DIR,
        as_supervised=True,
    )

    for image, _ in ds:
        image = tf.cast(image, tf.float32)
        image = tf.image.resize(image, [IMAGE_SIZE, IMAGE_SIZE])
        image = image / 255.0
        image = tf.expand_dims(image, axis=0)
        yield [image.numpy()]


def _save_label_map(class_names):
    """Save class names to a text file for TFLite metadata."""
    with open(LABEL_MAP_PATH, "w") as f:
        for name in class_names:
            # Convert snake_case to Title Case for display
            display_name = name.replace("_", " ").title()
            f.write(f"{display_name}\n")
    print(f"Label map saved to {LABEL_MAP_PATH} ({len(class_names)} classes)")
