#!/usr/bin/env bash
set -e

HOOK_PATH=".git/hooks/pre-commit"

cat > "$HOOK_PATH" << 'EOF'
#!/usr/bin/env bash
set -e

echo "🔍 Running ktlint format..."
./gradlew ktlintFormat --daemon -q

echo "🔍 Running Detekt..."
./gradlew detekt --daemon -q

# Re-add formatted files
git add -u

echo "✅ Pre-commit checks passed"
EOF

chmod +x "$HOOK_PATH"
echo "✅ Pre-commit hook installed at $HOOK_PATH"