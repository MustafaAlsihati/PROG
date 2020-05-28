/*
 * Copyright (c) PROG's Team (Mustafa AlSihati Team).
 * This Project is currently an academic project for educational purposes.
 * This Project May be used for benefits for the working team.
 * Fully owned by the application developers.
 */

package iau.mustafa.prog;

public class Project {
    public String projectName, description, DueDate, creator;

    public Project() { }

    public Project(String projectName, String description, String dueDate, String creator) {
        this.projectName = projectName;
        this.description = description;
        this.DueDate = dueDate;
        this.creator = creator;
    }
}
