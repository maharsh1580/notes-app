from typing import Annotated
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query, Response, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.database import get_session
from app.models import Note
from app.schemas import NoteCreate, NoteRead, NoteUpdate

router = APIRouter(prefix="/api/v1/notes", tags=["notes"])
Database = Annotated[Session, Depends(get_session)]


def find_note(note_id: UUID, session: Session) -> Note:
    note = session.get(Note, note_id)
    if note is None:
        raise HTTPException(status_code=404, detail="Note not found")
    return note


@router.get("", response_model=list[NoteRead])
def list_notes(
    session: Database,
    limit: Annotated[int, Query(ge=1, le=100)] = 100,
    offset: Annotated[int, Query(ge=0)] = 0,
):
    return session.scalars(
        select(Note).order_by(Note.created_at.desc(), Note.id.desc()).offset(offset).limit(limit)
    ).all()


@router.post("", response_model=NoteRead, status_code=status.HTTP_201_CREATED)
def create_note(payload: NoteCreate, session: Database, response: Response):
    note = Note(**payload.model_dump())
    session.add(note)
    session.commit()
    session.refresh(note)
    response.headers["Location"] = f"/api/v1/notes/{note.id}"
    return note


@router.get("/{note_id}", response_model=NoteRead)
def read_note(note_id: UUID, session: Database):
    return find_note(note_id, session)


@router.patch("/{note_id}", response_model=NoteRead)
def update_note(note_id: UUID, payload: NoteUpdate, session: Database):
    note = find_note(note_id, session)
    for key, value in payload.model_dump(exclude_unset=True).items():
        setattr(note, key, value)
    session.commit()
    session.refresh(note)
    return note


@router.delete("/{note_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_note(note_id: UUID, session: Database):
    session.delete(find_note(note_id, session))
    session.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
