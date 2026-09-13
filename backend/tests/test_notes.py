from uuid import uuid4

import pytest

URL = "/api/v1/notes"


def test_crud(client):
    assert client.get(URL).json() == []
    created = client.post(URL, json={"title": "  First  ", "content": "Hello"})
    assert created.status_code == 201
    note = created.json()
    assert note["title"] == "First"
    assert note["created_at"] and note["updated_at"]
    location = created.headers["location"]
    assert client.get(location).json() == note
    assert client.get(URL).json() == [note]
    changed = client.patch(location, json={"title": "Revised"})
    assert changed.status_code == 200
    assert changed.json()["content"] == "Hello"
    assert changed.json()["title"] == "Revised"
    assert changed.json()["updated_at"] >= note["updated_at"]
    assert client.patch(location, json={"content": ""}).json()["content"] == ""
    deleted = client.delete(location)
    assert deleted.status_code == 204 and not deleted.content
    assert client.get(location).status_code == 404
    assert client.get(URL).json() == []


@pytest.mark.parametrize(
    "payload",
    [
        {},
        {"title": " "},
        {"title": "a" * 201},
        {"title": None},
        {"title": "ok", "content": "x" * 10001},
        {"title": "ok", "extra": True},
    ],
)
def test_invalid_create(client, payload):
    assert client.post(URL, json=payload).status_code == 422


@pytest.mark.parametrize("payload", [{}, {"title": None}, {"content": None}, {"title": " "}])
def test_invalid_patch(client, payload):
    note = client.post(URL, json={"title": "Keep me"}).json()
    assert client.patch(f"{URL}/{note['id']}", json=payload).status_code == 422
    assert client.get(f"{URL}/{note['id']}").json()["title"] == "Keep me"


def test_missing_and_malformed_ids(client):
    path = f"{URL}/{uuid4()}"
    assert client.get(path).status_code == 404
    assert client.patch(path, json={"title": "Missing"}).status_code == 404
    assert client.delete(path).status_code == 404
    assert client.get(f"{URL}/not-a-uuid").status_code == 422


def test_pagination(client):
    for title in ("One", "Two", "Three"):
        assert client.post(URL, json={"title": title}).status_code == 201
    all_notes = client.get(URL).json()
    assert len(all_notes) == 3
    assert client.get(URL, params={"limit": 1, "offset": 1}).json() == all_notes[1:2]
    assert client.get(URL, params={"limit": 0}).status_code == 422
    assert client.get(URL, params={"offset": -1}).status_code == 422
