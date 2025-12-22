#!/bin/bash
# Script to pull Mistral 3B model in Ollama

echo "Pulling Mistral 3B model for Ollama..."
docker exec lm_ollama ollama pull mistral:3b

echo "Model pulled successfully!"
echo "You can now use the Literature Manager with local AI processing."
