package com.example.todo.dto;

import java.time.LocalDateTime;

public class TodoResponse {

    private Long id;
    private String title;
    private boolean done;
    private String memo;
    private LocalDateTime dateTime;
    private boolean notify;
    private boolean notified;
    private String listColor;
    private Long listId;
    private String listName;

    public TodoResponse(Long id, String title, boolean done, String memo, LocalDateTime dateTime, boolean notify, boolean notified, String listColor, Long listId, String listName) {
        this.id = id;
        this.title = title;
        this.done = done;
        this.memo = memo;
        this.dateTime = dateTime;
        this.notify = notify;
        this.notified = notified;
        this.listColor = listColor;
        this.listId = listId;
        this.listName = listName;  }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isDone() { return done; }
    public String getMemo() { return memo; }
    public LocalDateTime getDateTime() { return dateTime; }
    public boolean isNotify() { return notify; }
    public boolean isNotified() { return notified; }
    public String getListColor() { return listColor; }
    public Long getListId() { return listId; }
    public String getListName() { return listName; }
}