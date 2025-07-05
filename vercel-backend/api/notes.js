import { createClient } from '@supabase/supabase-js';

const supabaseUrl = process.env.SUPABASE_URL;
const supabaseAnonKey = process.env.SUPABASE_ANON_KEY;

const supabase = createClient(supabaseUrl, supabaseAnonKey);

export default async function handler(req, res) {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  try {
    switch (req.method) {
      case 'GET':
        // Get all notes
        const { data: notes, error: getError } = await supabase
          .from('notes')
          .select('*')
          .order('created_at', { ascending: false });

        if (getError) {
          return res.status(400).json({ error: getError.message });
        }

        return res.status(200).json({ notes });

      case 'POST':
        // Create a new note
        const { title, content } = req.body;

        if (!title || !content) {
          return res.status(400).json({ error: 'Title and content are required' });
        }

        const { data: newNote, error: postError } = await supabase
          .from('notes')
          .insert([{ title, content }])
          .select()
          .single();

        if (postError) {
          return res.status(400).json({ error: postError.message });
        }

        return res.status(201).json({ note: newNote });

      case 'PUT':
        // Update a note
        const { id } = req.query;
        const { title: updateTitle, content: updateContent } = req.body;

        if (!id) {
          return res.status(400).json({ error: 'Note ID is required' });
        }

        const { data: updatedNote, error: putError } = await supabase
          .from('notes')
          .update({ title: updateTitle, content: updateContent })
          .eq('id', id)
          .select()
          .single();

        if (putError) {
          return res.status(400).json({ error: putError.message });
        }

        return res.status(200).json({ note: updatedNote });

      case 'DELETE':
        // Delete a note
        const { id: deleteId } = req.query;

        if (!deleteId) {
          return res.status(400).json({ error: 'Note ID is required' });
        }

        const { error: deleteError } = await supabase
          .from('notes')
          .delete()
          .eq('id', deleteId);

        if (deleteError) {
          return res.status(400).json({ error: deleteError.message });
        }

        return res.status(200).json({ message: 'Note deleted successfully' });

      default:
        res.setHeader('Allow', ['GET', 'POST', 'PUT', 'DELETE']);
        return res.status(405).end(`Method ${req.method} Not Allowed`);
    }
  } catch (error) {
    console.error('API Error:', error);
    return res.status(500).json({ error: 'Internal server error' });
  }
}
