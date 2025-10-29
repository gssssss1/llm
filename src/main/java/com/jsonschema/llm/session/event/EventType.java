package com.jsonschema.llm.session.event;

public enum EventType {
    USER_MESSAGE,
    ASSISTANT_MESSAGE,
    SYSTEM_MESSAGE,
    TOOL_CALL,
    TOOL_RESULT,
    THINKING,
    ERROR,
    SESSION_START,
    SESSION_END
}
