#!/usr/bin/env python3
"""
Standalone prediction script for testing the food recognition model.

Can use either the Keras model or the exported TFLite model.

Usage:
    python predict.py path/to/food_image.jpg
    python predict.py path/to/food_image.jpg --tflite exported_model/whats_in_my_thattu_v2.tflite
    python predict.py path/to/food_image.jpg --top-k 10
"""

import argparse
import os
import sys

import numpy as np
from PIL import Image

from config import (
    CHECKPOINT_DIR,
    IMAGE_SIZE,
    LABEL_MAP_PATH,
    TFLITE_MODEL_PATH,
)


def load_labels(label_path=LABEL_MAP_PATH):
    """Load class labels from the label map file."""
    if not os.path.exists(label_path):
        print(f"Error: Label map not found at {label_path}")
        print("Run training first to generate labels.")
        sys.exit(1)

    with open(label_path, "r") as f:
        labels = [line.strip() for line in f.readlines()]
    return labels


def preprocess_image(image_path):
    """
    Load and preprocess an image for model input.

    Applies the same preprocessing used during training:
    resize to 224x224 and normalize to [0, 1].
    """
    img = Image.open(image_path).convert("RGB")
    img = img.resize((IMAGE_SIZE, IMAGE_SIZE), Image.LANCZOS)
    img_array = np.array(img, dtype=np.float32) / 255.0
    return np.expand_dims(img_array, axis=0)


def predict_keras(image_path, model_path, labels, top_k=5):
    """Run prediction using Keras model."""
    import tensorflow as tf

    model = tf.keras.models.load_model(model_path)
    input_data = preprocess_image(image_path)
    predictions = model.predict(input_data, verbose=0)[0]

    return _format_results(predictions, labels, top_k)


def predict_tflite(image_path, tflite_path, labels, top_k=5):
    """Run prediction using TFLite model."""
    import tensorflow as tf

    interpreter = tf.lite.Interpreter(model_path=tflite_path)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    input_data = preprocess_image(image_path)

    if input_details[0]["dtype"] == np.uint8:
        input_data = (input_data * 255).astype(np.uint8)
    else:
        input_data = input_data.astype(np.float32)

    interpreter.set_tensor(input_details[0]["index"], input_data)
    interpreter.invoke()
    predictions = interpreter.get_tensor(output_details[0]["index"])[0]

    return _format_results(predictions, labels, top_k)


def _format_results(predictions, labels, top_k):
    """Format raw predictions into sorted results."""
    top_indices = np.argsort(predictions)[::-1][:top_k]

    results = []
    for idx in top_indices:
        results.append({
            "label": labels[idx] if idx < len(labels) else f"class_{idx}",
            "confidence": float(predictions[idx]),
            "class_id": int(idx),
        })

    return results


def main():
    parser = argparse.ArgumentParser(
        description="Predict food items in an image"
    )
    parser.add_argument(
        "image_path",
        type=str,
        help="Path to the food image",
    )
    parser.add_argument(
        "--tflite",
        type=str,
        default=None,
        help="Path to .tflite model (uses TFLite inference)",
    )
    parser.add_argument(
        "--checkpoint",
        type=str,
        default=os.path.join(CHECKPOINT_DIR, "final_model.keras"),
        help="Path to Keras model checkpoint",
    )
    parser.add_argument(
        "--labels",
        type=str,
        default=LABEL_MAP_PATH,
        help="Path to labels text file",
    )
    parser.add_argument(
        "--top-k",
        type=int,
        default=5,
        help="Number of top predictions to show (default: 5)",
    )
    args = parser.parse_args()

    if not os.path.exists(args.image_path):
        print(f"Error: Image not found at {args.image_path}")
        sys.exit(1)

    labels = load_labels(args.labels)

    print(f"Image: {args.image_path}")
    print(f"Labels loaded: {len(labels)} classes")

    if args.tflite:
        print(f"Model: {args.tflite} (TFLite)")
        results = predict_tflite(args.image_path, args.tflite, labels, args.top_k)
    else:
        print(f"Model: {args.checkpoint} (Keras)")
        results = predict_keras(args.image_path, args.checkpoint, labels, args.top_k)

    print(f"\nTop {args.top_k} predictions:")
    print("-" * 45)
    for i, r in enumerate(results, 1):
        bar = "#" * int(r["confidence"] * 30)
        print(f"  {i}. {r['label']:25s} {r['confidence']*100:6.2f}% {bar}")


if __name__ == "__main__":
    main()
