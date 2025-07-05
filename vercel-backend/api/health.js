const apiKey = process.env.API_KEY; // Secret API key for authentication

// Middleware function to verify API key
function verifyApiKey(req) {
  const providedKey = req.headers['x-api-key'] || req.headers['authorization']?.replace('Bearer ', '');
  
  if (!apiKey) {
    throw new Error('API_KEY environment variable not configured');
  }
  
  if (!providedKey) {
    return { valid: false, message: 'API key is required. Provide it via x-api-key header or Authorization header.' };
  }
  
  if (providedKey !== apiKey) {
    return { valid: false, message: 'Invalid API key' };
  }
  
  return { valid: true };
}

export default function handler(req, res) {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, x-api-key');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  // Verify API key for all requests except OPTIONS
  try {
    const keyVerification = verifyApiKey(req);
    if (!keyVerification.valid) {
      return res.status(401).json({ error: keyVerification.message });
    }
  } catch (error) {
    console.error('API Key verification error:', error);
    return res.status(500).json({ error: 'Authentication configuration error' });
  }

  if (req.method === 'GET') {
    return res.status(200).json({ 
      status: 'healthy', 
      message: 'SimpleNote API is running',
      timestamp: new Date().toISOString()
    });
  }

  res.setHeader('Allow', ['GET']);
  return res.status(405).end(`Method ${req.method} Not Allowed`);
}
