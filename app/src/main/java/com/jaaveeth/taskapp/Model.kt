package com.jaaveeth.taskapp

class Model {
    var task: String? = null
    var description: String? = null
    var id: String? = null
    var date: String? = null
    var status: Int? = 0

    constructor()
    constructor(task: String?, description: String?, id: String?, date: String?, status: Int = 0) {
        this.task = task
        this.description = description
        this.id = id
        this.status = status
        this.date = date
    }
}
