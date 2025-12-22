#!/bin/bash
# Script to pull Ministral 3B model in Ollama

echo "Pulling Ministral 3B model for Ollama..."
docker exec lm_ollama ollama pull ministral:3b

echo "Model pulled successfully!"
echo "You can now use the Literature Manager with local AI processing."
