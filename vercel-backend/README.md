# SimpleNote Backend API

This is the backend API for the SimpleNote Android application, deployed on Vercel.

## Base URL

```
https://vercel-backend-inky-xi.vercel.app
```

## API Endpoints

### Health Check
- **GET** `/api/health`
- Returns the API status

### Notes Management
- **GET** `/api/notes` - Get all notes
- **POST** `/api/notes` - Create a new note
- **PUT** `/api/notes?id={noteId}` - Update a note
- **DELETE** `/api/notes?id={noteId}` - Delete a note

## Request/Response Format

### Create Note (POST /api/notes)
```json
{
  "title": "Note Title",
  "content": "Note content"
}
```

### Update Note (PUT /api/notes?id=123)
```json
{
  "title": "Updated Title",
  "content": "Updated content"
}
```

### Response Format
```json
{
  "note": {
    "id": 1,
    "title": "Note Title",
    "content": "Note content",
    "created_at": "2025-07-05T21:28:55.136Z",
    "updated_at": "2025-07-05T21:28:55.136Z"
  }
}
```

## Environment Variables

- `SUPABASE_URL` - Your Supabase project URL
- `SUPABASE_ANON_KEY` - Your Supabase anonymous key

## Database Schema

Before using the API, make sure to create the `notes` table in your Supabase database:

```sql
CREATE TABLE notes (
  id BIGSERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE notes ENABLE ROW LEVEL SECURITY;

-- Create a policy that allows all operations (for testing)
CREATE POLICY "Allow all operations on notes" ON notes FOR ALL USING (true);
```
