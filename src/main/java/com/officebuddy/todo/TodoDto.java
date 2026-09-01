package com.officebuddy.todo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoDto {
    private String id;
    private String userId;
    private String goalId;
    private String title;
    private String content;
    private String type;
    private String dueDate;
    private boolean completed;
    private String createdAt;
}
