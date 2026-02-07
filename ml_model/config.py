"""
Configuration for the What's In My Thattu food recognition model.

This config defines all hyperparameters, paths, and model settings
for training a food classifier using transfer learning.
"""

import os

# --- Paths ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "data")
CHECKPOINT_DIR = os.path.join(BASE_DIR, "checkpoints")
EXPORT_DIR = os.path.join(BASE_DIR, "exported_model")
LABEL_MAP_PATH = os.path.join(EXPORT_DIR, "labels.txt")
TFLITE_MODEL_PATH = os.path.join(EXPORT_DIR, "whats_in_my_thattu_v2.tflite")
TFLITE_QUANTIZED_PATH = os.path.join(EXPORT_DIR, "whats_in_my_thattu_v2_quantized.tflite")

# --- Dataset ---
DATASET_NAME = "food101"
NUM_CLASSES = 101
VALIDATION_SPLIT = 0.15  # 15% of training set used for validation

# --- Image preprocessing ---
IMAGE_SIZE = 224  # EfficientNetV2B0 default input size
CROP_PADDING = 32  # Padding before center crop during eval
CHANNELS = 3

# --- Training hyperparameters ---
BATCH_SIZE = 32
EPOCHS = 30
INITIAL_LEARNING_RATE = 1e-3
FINE_TUNE_LEARNING_RATE = 1e-5
FINE_TUNE_EPOCHS = 15
FINE_TUNE_AT_LAYER = 100  # Unfreeze layers from this index onwards

# --- Regularization ---
DROPOUT_RATE = 0.3
LABEL_SMOOTHING = 0.1
WEIGHT_DECAY = 1e-5

# --- Data augmentation ---
AUGMENTATION_CONFIG = {
    "random_flip": "horizontal",
    "random_rotation": 0.1,        # 10% of 2*pi
    "random_zoom": (-0.15, 0.15),  # Zoom range
    "random_brightness": 0.2,
    "random_contrast": (0.8, 1.2),
}

# --- Callbacks ---
EARLY_STOPPING_PATIENCE = 5
REDUCE_LR_PATIENCE = 3
REDUCE_LR_FACTOR = 0.5
MIN_LEARNING_RATE = 1e-7

# --- TFLite export ---
QUANTIZE_MODEL = True  # Apply post-training dynamic range quantization
REPRESENTATIVE_DATASET_SIZE = 200  # Samples for full integer quantization

# --- Model metadata ---
MODEL_NAME = "What's In My Thattu Food Classifier"
MODEL_DESCRIPTION = (
    "Food recognition model trained on Food-101 dataset using "
    "EfficientNetV2B0 transfer learning. Classifies images into "
    "101 food categories with improved accuracy over the baseline model."
)
MODEL_VERSION = "2.0.0"
MODEL_AUTHOR = "What's In My Thattu"
MODEL_LICENSE = "Apache-2.0"
