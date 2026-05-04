const jsonParse = JSON.parse as (value: string) => unknown;

export const parseJsonObject = (value?: string | Record<string, unknown>) => {
  if (!value) return {} as Record<string, unknown>;
  if (typeof value !== 'string') return value;

  try {
    return jsonParse(value) as Record<string, unknown>;
  } catch (error) {
    console.error('Parse JSON object failed:', error, value);
    return {} as Record<string, unknown>;
  }
};

export const parseJsonArray = (value?: string | string[]) => {
  if (Array.isArray(value)) return value.flat().map(String);
  if (!value) return [] as string[];

  try {
    const parsed = jsonParse(value);
    return Array.isArray(parsed) ? parsed.flat().map(String) : [];
  } catch (error) {
    console.error('Parse JSON array failed:', error, value);
    return [] as string[];
  }
};
