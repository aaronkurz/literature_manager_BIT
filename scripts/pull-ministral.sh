#!/bin/bash
# Script to pull Ministral-3 (3B) model in Ollama
# Note: Requires Ollama 0.13.1+ (pre-release)

echo "Pulling Ministral-3 (3B) model for Ollama..."
echo "This model has a 256K context window and supports vision, multilingual, and function calling."
docker exec lm_ollama ollama pull ministral-3:3b

echo ""
echo "Model pulled successfully!"
echo "Model size: 3GB"
echo "Context window: 256K tokens"
echo "You can now use the Literature Manager with local AI processing."
