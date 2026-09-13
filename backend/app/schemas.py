from datetime import datetime
from typing import Annotated
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field, StringConstraints, model_validator

Title = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=200)]
Content = Annotated[str, Field(max_length=10000)]


class NoteCreate(BaseModel):
    model_config = ConfigDict(extra="forbid")
    title: Title
    content: Content = ""


class NoteUpdate(BaseModel):
    model_config = ConfigDict(extra="forbid")
    title: Title | None = None
    content: Content | None = None

    @model_validator(mode="after")
    def reject_empty_or_null(self):
        if not self.model_fields_set:
            raise ValueError("Provide title or content")
        if any(getattr(self, field) is None for field in self.model_fields_set):
            raise ValueError("Note fields cannot be null")
        return self


class NoteRead(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: UUID
    title: str
    content: str
    created_at: datetime
    updated_at: datetime
