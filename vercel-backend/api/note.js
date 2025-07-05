import { createClient } from '@supabase/supabase-js';

const supabase = createClient(
  process.env.SUPABASE_URL,
  process.env.SUPABASE_ANON_KEY
);

// API Key authentication middleware
function authenticateRequest(req) {
  const apiKey = req.headers['x-api-key'] || 
                (req.headers.authorization && req.headers.authorization.replace('Bearer ', ''));
  
  if (!apiKey) {
    return {
      isValid: false,
      error: { 
        error: 'API key is required. Provide it via x-api-key header or Authorization header.' 
      }
    };
  }
  
  if (apiKey !== process.env.API_KEY) {
    return {
      isValid: false,
      error: { error: 'Invalid API key' }
    };
  }
  
  return { isValid: true };
}

export default async function handler(req, res) {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, x-api-key');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  // Authenticate request
  const authResult = authenticateRequest(req);
  if (!authResult.isValid) {
    return res.status(401).json(authResult.error);
  }

  if (req.method === 'GET') {
    try {
      const { id } = req.query;
      
      if (!id) {
        return res.status(400).json({ error: 'Note ID is required' });
      }

      const { data: note, error } = await supabase
        .from('notes')
        .select('*')
        .eq('id', id)
        .single();

      if (error) {
        if (error.code === 'PGRST116') {
          return res.status(404).json({ error: 'Note not found' });
        }
        console.error('Supabase error:', error);
        return res.status(500).json({ error: 'Failed to fetch note' });
      }

      return res.status(200).json({ note });

    } catch (error) {
      console.error('API error:', error);
      return res.status(500).json({ error: 'Internal server error' });
    }
  }

  return res.status(405).json({ error: 'Method not allowed' });
}
