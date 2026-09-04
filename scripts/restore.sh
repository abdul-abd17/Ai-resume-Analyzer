#!/bin/bash
# ResuMatch Disaster Recovery Restore Script
set -e

if [ -z "$1" ]; then
  echo "Usage: ./scripts/restore.sh <BACKUP_DIR_PATH>"
  exit 1
fi

BACKUP_DIR="$1"

echo "[RESTORE] Starting ResuMatch Disaster Recovery Restore from $BACKUP_DIR..."

# 1. Restore Database
if [ -f "$BACKUP_DIR/database.sql" ]; then
  echo "[RESTORE] Restoring PostgreSQL database dump..."
  cat "$BACKUP_DIR/database.sql" | docker exec -i resumatch-postgres psql -U postgres -d airesume_db
fi

# 2. Restore Uploads
if [ -f "$BACKUP_DIR/uploads_backup.tar.gz" ]; then
  echo "[RESTORE] Extracting uploaded files..."
  mkdir -p ./backend/uploads
  tar -xzf "$BACKUP_DIR/uploads_backup.tar.gz" -C ./backend/uploads
fi

echo "[RESTORE] Restoration completed successfully!"
