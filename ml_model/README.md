# What's In My Thattu — ML Model Training Pipeline

This directory contains the training pipeline for the food recognition model
used in the What's In My Thattu Android app.

## Why a New Model?

The original app uses Google's AIY Vision Food Classifier V1 (`aiy/vision-classifier-food-v1`),
which is based on an older MobileNet architecture. This new pipeline trains a model using
**EfficientNetV2B0** with transfer learning, which provides:

- Higher baseline accuracy (EfficientNetV2B0: ~78.7% ImageNet top-1 vs ~70% for the original)
- Better fine-grained food feature extraction via two-phase training
- Proper data augmentation to reduce overfitting
- Consistent preprocessing between training and inference
- Quantized model for efficient on-device inference

## Architecture

```
EfficientNetV2B0 (pretrained ImageNet, 224x224 input)
  → GlobalAveragePooling2D
  → BatchNorm → Dense(512, ReLU, L2) → Dropout(0.3)
  → BatchNorm → Dense(256, ReLU, L2) → Dropout(0.3)
  → Dense(101, Softmax)
```

## Training Strategy

### Phase 1: Transfer Learning
- Freeze EfficientNetV2B0 base model
- Train only the classification head
- Learning rate: 1e-3 with Adam optimizer
- 30 epochs with early stopping

### Phase 2: Fine-Tuning
- Unfreeze top ~130 layers of the base model
- Train end-to-end with much lower learning rate (1e-5)
- 15 epochs with early stopping
- Label smoothing (0.1) to prevent overconfidence

## Dataset

Uses [Food-101](https://data.vision.ee.ethz.ch/cvl/datasets_extra/food-101/) via
TensorFlow Datasets:
- 101 food categories
- 75,750 training images (split 85/15 for train/val)
- 25,250 test images
- Data augmentation: random flip, rotation, zoom, brightness, contrast

## Quick Start

### 1. Install Dependencies

```bash
cd ml_model
pip install -r requirements.txt
```

### 2. Train the Model

```bash
python train.py
```

Options:
```bash
python train.py --epochs 50 --fine-tune-epochs 20 --batch-size 64
python train.py --fine-tune-only --checkpoint checkpoints/best_phase1_transfer.keras
python train.py --gpu 0
```

### 3. Export to TFLite

```bash
python export_tflite.py
```

Options:
```bash
# Full float32 (no quantization)
python export_tflite.py --no-quantize

# Full integer quantization (smallest model)
python export_tflite.py --full-integer-quantization
```

### 4. Evaluate

```bash
python evaluate.py
python evaluate.py --tflite exported_model/whats_in_my_thattu_v2.tflite
```

### 5. Test Predictions

```bash
python predict.py path/to/food_image.jpg
python predict.py path/to/food_image.jpg --tflite exported_model/whats_in_my_thattu_v2.tflite --top-k 10
```

## Deploy to Android

1. Copy the exported model:
   ```bash
   cp exported_model/whats_in_my_thattu_v2.tflite \
     ../tensorImageInterpreter/src/main/ml/
   ```

2. Update `TensorImageInterpreter.kt` to reference the new model class name
   (Android's ML Model Binding auto-generates it from the filename)

3. Rebuild the Android app

## File Structure

```
ml_model/
├── config.py           # All hyperparameters and paths
├── dataset.py          # Food-101 loading and augmentation pipeline
├── model.py            # EfficientNetV2B0 transfer learning architecture
├── train.py            # Two-phase training script
├── export_tflite.py    # TFLite conversion with quantization and metadata
├── evaluate.py         # Detailed evaluation with per-class metrics
├── predict.py          # Standalone image prediction utility
├── requirements.txt    # Python dependencies
├── data/               # Downloaded dataset (gitignored)
├── checkpoints/        # Training checkpoints (gitignored)
└── exported_model/     # Exported TFLite models (gitignored)
```

## Hardware Requirements

- **Training**: GPU recommended (NVIDIA with CUDA). CPU training will work but is slow.
- **Inference**: The exported TFLite model runs on any Android device (CPU or GPU delegate).
- **Storage**: ~5 GB for the Food-101 dataset download.
