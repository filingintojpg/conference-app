package com.maltsev.conference_app.exception;

import java.util.Map;

public record ValidationErrorResponse(String error, Map<String, String> fields) {}
