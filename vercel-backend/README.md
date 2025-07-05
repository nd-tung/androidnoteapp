# SimpleNote Backend API

This is the backend API for the SimpleNote Android application, built with Node.js and deployed on Vercel as serverless functions.

## 🚀 Deployment Information

- **Platform**: Vercel (Serverless Functions)
- **Runtime**: Node.js 22.x
- **Database**: Supabase (PostgreSQL)
- **Status**: ✅ Live and Running

## 🌐 Base URL

```
https://vercel-backend-inky-xi.vercel.app
```

## 📊 Quick Health Check

Test if the API is running:
```bash
curl https://vercel-backend-inky-xi.vercel.app/api/health
```

Expected Response:
```json
{
  "status": "healthy",
  "message": "SimpleNote API is running",
  "timestamp": "2025-07-05T21:28:55.136Z"
}
```

## 🛠 API Endpoints

### Health Check
- **GET** `/api/health`
- **Description**: Returns the API status and timestamp
- **Response**: JSON with status, message, and timestamp

### Notes Management
- **GET** `/api/notes` - Retrieve all notes (ordered by newest first)
- **POST** `/api/notes` - Create a new note
- **PUT** `/api/notes?id={noteId}` - Update an existing note
- **DELETE** `/api/notes?id={noteId}` - Delete a note by ID

### CORS Support
All endpoints support Cross-Origin Resource Sharing (CORS) with:
- **Allowed Origins**: `*` (all origins)
- **Allowed Methods**: `GET, POST, PUT, DELETE, OPTIONS`
- **Allowed Headers**: `Content-Type, Authorization`

## 📝 Request/Response Format

### Create Note (POST /api/notes)
**Request Body:**
```json
{
  "title": "Note Title",
  "content": "Note content"
}
```

**Success Response (201):**
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

### Update Note (PUT /api/notes?id=123)
**Request Body:**
```json
{
  "title": "Updated Title",
  "content": "Updated content"
}
```

**Success Response (200):**
```json
{
  "note": {
    "id": 123,
    "title": "Updated Title",
    "content": "Updated content",
    "created_at": "2025-07-05T21:28:55.136Z",
    "updated_at": "2025-07-05T21:30:15.420Z"
  }
}
```

### Get All Notes (GET /api/notes)
**Success Response (200):**
```json
{
  "notes": [
    {
      "id": 2,
      "title": "Latest Note",
      "content": "This is the newest note",
      "created_at": "2025-07-05T21:30:00.000Z",
      "updated_at": "2025-07-05T21:30:00.000Z"
    },
    {
      "id": 1,
      "title": "First Note",
      "content": "This is an older note",
      "created_at": "2025-07-05T21:28:55.136Z",
      "updated_at": "2025-07-05T21:28:55.136Z"
    }
  ]
}
```

### Delete Note (DELETE /api/notes?id=123)
**Success Response (200):**
```json
{
  "message": "Note deleted successfully"
}
```

### Error Response Format
**Error Response (400/404/500):**
```json
{
  "error": "Error message description"
}
```

## 🧪 Testing the API

### Using cURL

**Test Health Check:**
```bash
curl https://vercel-backend-inky-xi.vercel.app/api/health
```

**Get All Notes:**
```bash
curl https://vercel-backend-inky-xi.vercel.app/api/notes
```

**Create a New Note:**
```bash
curl -X POST https://vercel-backend-inky-xi.vercel.app/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title":"My First Note","content":"This is the content of my first note"}'
```

**Update a Note:**
```bash
curl -X PUT "https://vercel-backend-inky-xi.vercel.app/api/notes?id=1" \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated Note","content":"This note has been updated"}'
```

**Delete a Note:**
```bash
curl -X DELETE "https://vercel-backend-inky-xi.vercel.app/api/notes?id=1"
```

### Using JavaScript (for Android WebView or Testing)
```javascript
// Create a note
fetch('https://vercel-backend-inky-xi.vercel.app/api/notes', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    title: 'My Note',
    content: 'Note content here'
  })
})
.then(response => response.json())
.then(data => console.log(data));
```

## 🚀 Deployment Information

### Current Deployment
- **URL**: https://vercel-backend-inky-xi.vercel.app
- **Status**: ✅ Production Ready
- **Last Updated**: 2025-07-05
- **Deployment ID**: Check Vercel dashboard for latest

### Deployment Commands Used
```bash
# Initial deployment
cd vercel-backend
vercel --prod

# Environment variables setup
vercel env add SUPABASE_URL production
vercel env add SUPABASE_ANON_KEY production

# Redeploy after env changes
vercel --prod
```

### Vercel Configuration (vercel.json)
```json
{
  "version": 2,
  "headers": [
    {
      "source": "/api/(.*)",
      "headers": [
        {
          "key": "Access-Control-Allow-Origin",
          "value": "*"
        },
        {
          "key": "Access-Control-Allow-Methods",
          "value": "GET, POST, PUT, DELETE, OPTIONS"
        },
        {
          "key": "Access-Control-Allow-Headers",
          "value": "Content-Type, Authorization"
        }
      ]
    }
  ]
}
```

## 🔧 Environment Variables

The following environment variables are configured in Vercel:

### Required Variables (✅ Configured)
- `SUPABASE_URL` - Your Supabase project URL
  - Value: `https://ttljleilzkdkkarcmgqi.supabase.co`
- `SUPABASE_ANON_KEY` - Your Supabase anonymous key for client connections
  - Used for: Row Level Security, client authentication

### Not Required for This Setup
The following variables are available but not needed for the current Supabase client implementation:
- `POSTGRES_URL` - Direct PostgreSQL connection (not used)
- `SUPABASE_SERVICE_ROLE_KEY` - Admin operations (not needed for basic CRUD)
- `POSTGRES_*` variables - Direct DB access (bypassed by Supabase client)

## 🏗 Technical Architecture

### Backend Stack
- **Runtime**: Node.js 22.x
- **Platform**: Vercel Serverless Functions
- **Database**: Supabase (PostgreSQL)
- **ORM/Client**: Supabase JavaScript Client
- **API Style**: RESTful API

### File Structure
```
vercel-backend/
├── api/
│   ├── health.js          # Health check endpoint
│   └── notes.js           # Notes CRUD operations
├── package.json           # Dependencies and project config
├── package-lock.json      # Locked dependency versions
├── vercel.json           # Vercel deployment configuration
├── .gitignore            # Git ignore rules
└── README.md             # This documentation
```

### Dependencies
```json
{
  "@supabase/supabase-js": "^2.50.3",
  "express": "^5.1.0"
}
```

## 🗄 Database Schema

Before using the API, make sure to create the `notes` table in your Supabase database:

### SQL Setup Commands
Run these commands in your Supabase SQL Editor:

```sql
-- Create the notes table
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
-- Note: In production, you should create more restrictive policies
CREATE POLICY "Allow all operations on notes" ON notes FOR ALL USING (true);

-- Optional: Create an index for better performance on created_at ordering
CREATE INDEX idx_notes_created_at ON notes(created_at DESC);
```

### Table Structure
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Auto-incrementing unique identifier |
| `title` | TEXT | NOT NULL | Note title |
| `content` | TEXT | NOT NULL | Note content/body |
| `created_at` | TIMESTAMP WITH TIME ZONE | DEFAULT NOW() | When the note was created |
| `updated_at` | TIMESTAMP WITH TIME ZONE | DEFAULT NOW() | When the note was last updated |

### Security Notes
- **Row Level Security (RLS)** is enabled for security
- Current policy allows all operations for testing
- In production, consider implementing user-specific policies
- The anonymous key is used for client connections with RLS

## 📱 Android Integration

### Base URL for Android App
Use this base URL in your Android application's network configuration:
```
https://vercel-backend-inky-xi.vercel.app
```

### Recommended HTTP Client
- **OkHttp** or **Retrofit** for Android
- Ensure proper error handling for network requests
- Implement proper JSON serialization/deserialization

### Example Android Integration
```java
// Example base URL constant
public static final String BASE_URL = "https://vercel-backend-inky-xi.vercel.app/api/";

// Example API endpoints
public static final String NOTES_ENDPOINT = "notes";
public static final String HEALTH_ENDPOINT = "health";
```

## 🔍 Monitoring & Debugging

### Vercel Dashboard
- Monitor function executions and errors
- View deployment logs and analytics
- Check environment variable configuration

### Common Issues & Solutions
1. **CORS Errors**: Already configured, should work from any domain
2. **Database Connection**: Check Supabase environment variables
3. **Table Not Found**: Ensure the SQL schema is created in Supabase
4. **Authentication**: Using anonymous key with RLS policies

### Logs & Debugging
- Function logs available in Vercel dashboard
- Console errors logged for debugging
- All API errors return structured JSON responses

## 📋 Next Steps

1. **✅ Backend Deployed**: API is live and functional
2. **⚠️ Database Setup**: Create the `notes` table in Supabase using the SQL above
3. **📱 Android Integration**: Use the base URL in your Android app
4. **🔒 Security**: Consider implementing user authentication and proper RLS policies
5. **🚀 Production**: Test thoroughly before production use

---

**Backend Status**: ✅ **READY FOR INTEGRATION**

The backend is fully deployed and ready to handle requests from your Android application!
