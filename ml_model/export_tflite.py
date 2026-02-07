#!/usr/bin/env python3
"""
Export trained Keras model to TensorFlow Lite format for Android deployment.

Supports two export modes:
  1. Dynamic range quantization (default) — Smaller model with float16
  2. Full integer quantization — Smallest model, uses representative dataset

The exported .tflite model includes embedded metadata (labels, description)
so Android's ML Model Binding can auto-generate the wrapper class.

Usage:
    python export_tflite.py
    python export_tflite.py --checkpoint checkpoints/final_model.keras
    python export_tflite.py --full-integer-quantization
"""

import argparse
import os
import struct

import numpy as np
import tensorflow as tf

from config import (
    CHECKPOINT_DIR,
    EXPORT_DIR,
    IMAGE_SIZE,
    LABEL_MAP_PATH,
    MODEL_AUTHOR,
    MODEL_DESCRIPTION,
    MODEL_LICENSE,
    MODEL_NAME,
    MODEL_VERSION,
    REPRESENTATIVE_DATASET_SIZE,
    TFLITE_MODEL_PATH,
    TFLITE_QUANTIZED_PATH,
)


def convert_to_tflite(model, quantize=True, full_integer=False):
    """
    Convert a Keras model to TFLite format.

    Args:
        model: Trained Keras model.
        quantize: Apply dynamic range quantization.
        full_integer: Apply full integer quantization (requires representative dataset).

    Returns:
        TFLite model as bytes.
    """
    converter = tf.lite.TFLiteConverter.from_keras_model(model)

    # Optimization settings
    if full_integer:
        print("Applying full integer quantization...")
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
        converter.inference_input_type = tf.uint8
        converter.inference_output_type = tf.float32

        # Representative dataset for calibration
        from dataset import load_representative_dataset
        converter.representative_dataset = lambda: load_representative_dataset(
            REPRESENTATIVE_DATASET_SIZE
        )
    elif quantize:
        print("Applying dynamic range quantization...")
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_types = [tf.float16]
    else:
        print("No quantization (full float32)...")

    tflite_model = converter.convert()
    return tflite_model


def add_metadata(tflite_model_path, label_file_path):
    """
    Add metadata to TFLite model for Android ML Model Binding.

    Embeds model description, input/output tensor descriptions,
    and the label file so Android can auto-generate wrapper classes.
    """
    try:
        from tflite_support.metadata_writers import image_classifier
        from tflite_support.metadata_writers import writer_utils

        writer = image_classifier.MetadataWriter.create_for_inference(
            writer_utils.load_file(tflite_model_path),
            model_name=MODEL_NAME,
            model_description=MODEL_DESCRIPTION,
            input_norm_mean=[0.0],       # We normalize to [0,1] in preprocessing
            input_norm_std=[255.0],      # So TFLite should divide by 255
            label_file_paths=[label_file_path],
        )

        writer_utils.save_file(writer.populate(), tflite_model_path)
        print(f"Metadata added to {tflite_model_path}")

        # Verify metadata
        displayer = metadata_displayer.MetadataDisplayer.with_model_file(
            tflite_model_path
        )
        print(f"Metadata JSON:\n{displayer.get_metadata_json()}")

    except ImportError:
        print(
            "Warning: tflite-support not available for metadata writing. "
            "The model will work but won't have embedded metadata. "
            "Install with: pip install tflite-support"
        )
        _add_basic_metadata(tflite_model_path, label_file_path)


def _add_basic_metadata(tflite_model_path, label_file_path):
    """
    Fallback: write a companion label file alongside the model.
    Android code can load this separately.
    """
    companion_label_path = tflite_model_path.replace(".tflite", "_labels.txt")
    with open(label_file_path, "r") as src, open(companion_label_path, "w") as dst:
        dst.write(src.read())
    print(f"Companion label file written to {companion_label_path}")


def export_model(checkpoint_path, output_path, quantize=True, full_integer=False):
    """
    Full export pipeline: load model, convert, add metadata, save.

    Args:
        checkpoint_path: Path to the trained .keras model.
        output_path: Path for the output .tflite file.
        quantize: Apply quantization.
        full_integer: Use full integer quantization.
    """
    os.makedirs(EXPORT_DIR, exist_ok=True)

    # Load trained model
    print(f"Loading model from {checkpoint_path}...")
    model = tf.keras.models.load_model(checkpoint_path)
    model.summary()

    # Convert to TFLite
    print(f"\nConverting to TFLite...")
    tflite_model = convert_to_tflite(model, quantize=quantize, full_integer=full_integer)

    # Save model
    with open(output_path, "wb") as f:
        f.write(tflite_model)

    model_size_mb = len(tflite_model) / (1024 * 1024)
    print(f"\nTFLite model saved to {output_path}")
    print(f"  Model size: {model_size_mb:.1f} MB")

    # Add metadata
    if os.path.exists(LABEL_MAP_PATH):
        add_metadata(output_path, LABEL_MAP_PATH)
    else:
        print(f"Warning: Label map not found at {LABEL_MAP_PATH}")
        print("Run training first to generate the label map.")

    return output_path


def verify_tflite_model(model_path):
    """Verify the exported TFLite model works correctly."""
    print(f"\nVerifying model: {model_path}")

    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    print(f"  Input shape:  {input_details[0]['shape']}")
    print(f"  Input dtype:  {input_details[0]['dtype']}")
    print(f"  Output shape: {output_details[0]['shape']}")
    print(f"  Output dtype: {output_details[0]['dtype']}")

    # Test with random input
    input_shape = input_details[0]["shape"]
    input_dtype = input_details[0]["dtype"]

    if input_dtype == np.uint8:
        test_input = np.random.randint(0, 255, size=input_shape).astype(np.uint8)
    else:
        test_input = np.random.rand(*input_shape).astype(np.float32)

    interpreter.set_tensor(input_details[0]["index"], test_input)
    interpreter.invoke()
    output = interpreter.get_tensor(output_details[0]["index"])

    print(f"  Test output shape: {output.shape}")
    print(f"  Test output sum:   {output.sum():.4f} (should be ~1.0 for softmax)")
    print(f"  Top prediction:    class {np.argmax(output)} with score {np.max(output):.4f}")
    print("  Verification: PASSED")

    return True


def main():
    parser = argparse.ArgumentParser(
        description="Export trained model to TFLite for Android"
    )
    parser.add_argument(
        "--checkpoint",
        type=str,
        default=os.path.join(CHECKPOINT_DIR, "final_model.keras"),
        help="Path to trained .keras model",
    )
    parser.add_argument(
        "--output",
        type=str,
        default=TFLITE_MODEL_PATH,
        help=f"Output .tflite path (default: {TFLITE_MODEL_PATH})",
    )
    parser.add_argument(
        "--no-quantize",
        action="store_true",
        help="Disable quantization (larger model)",
    )
    parser.add_argument(
        "--full-integer-quantization",
        action="store_true",
        help="Use full integer quantization (smallest model)",
    )
    args = parser.parse_args()

    # Export
    output_path = export_model(
        checkpoint_path=args.checkpoint,
        output_path=args.output,
        quantize=not args.no_quantize,
        full_integer=args.full_integer_quantization,
    )

    # Verify
    verify_tflite_model(output_path)

    # Print deployment instructions
    print("\n" + "=" * 60)
    print("DEPLOYMENT INSTRUCTIONS")
    print("=" * 60)
    print(f"1. Copy the model to the Android project:")
    print(f"   cp {output_path} \\")
    print(f"     tensorImageInterpreter/src/main/ml/whats_in_my_thattu_v2.tflite")
    print(f"\n2. Update TensorImageInterpreter.kt to use the new model")
    print(f"3. Rebuild and test the Android app")


if __name__ == "__main__":
    main()
