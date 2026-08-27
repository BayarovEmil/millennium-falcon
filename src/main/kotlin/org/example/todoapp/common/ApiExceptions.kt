package org.example.todoapp.common

class NotFoundException(message: String) : RuntimeException(message)

class DomainRuleException(message: String) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)
