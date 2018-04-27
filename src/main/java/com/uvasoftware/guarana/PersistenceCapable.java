package com.uvasoftware.guarana;

public interface PersistenceCapable {
    void persist(String path, String contents);
}
