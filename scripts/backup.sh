#!/bin/bash
# ResuMatch Automated Production Backup Script
set -e

BACKUP_DIR="./backups/$(date +%Y-%m-%d_%H-%M-%S)"
mkdir -p "$BACKUP_DIR"

echo "[BACKUP] Starting ResuMatch Production Backup..."

# 1. PostgreSQL Database Dump
echo "[BACKUP] Dumping PostgreSQL database..."
docker exec resumatch-postgres pg_dump -U postgres airesume_db > "$BACKUP_DIR/database.sql"

# 2. Archive Uploaded Resumes & Reports
echo "[BACKUP] Archiving user uploads..."
tar -czf "$BACKUP_DIR/uploads_backup.tar.gz" -C ./backend/uploads .

echo "[BACKUP] Backup completed successfully at: $BACKUP_DIR"
