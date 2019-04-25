package de.codingair.warpsystem.spigot.base.editor;

public abstract class Backup<T> {
    private T backup;

    public Backup(T backup) {
        this.backup = backup;
    }

    public abstract void applyTo(T value);

    public T getBackup() {
        return backup;
    }
}
