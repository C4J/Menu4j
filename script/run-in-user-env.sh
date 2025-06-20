#!/bin/bash

# Determine which shell is being used
SHELL_NAME=$(basename "$SHELL")
TERM=xterm-256color

# Load the right environment
case "$SHELL_NAME" in
  zsh)
    source "$HOME/.zprofile" 2>/dev/null
    source "$HOME/.zshrc" 2>/dev/null
    ;;
  bash)
    source "$HOME/.bash_profile" 2>/dev/null
    source "$HOME/.bashrc" 2>/dev/null
    ;;
  *)
    echo "Unknown shell: $SHELL_NAME. Running without sourcing shell configs."
    ;;
esac

# Execute the passed command or script
exec "$@"
