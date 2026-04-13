/**
 * Normalizes Spring validation maps and ApiResponse bodies into a single string.
 */
export function getErrorMessage(error) {
  const res = error.response;
  if (!res?.data) {
    return error.message || 'Something went wrong';
  }
  const data = res.data;
  if (typeof data.message === 'string') {
    return data.message;
  }
  if (typeof data === 'object' && data !== null) {
    const vals = Object.values(data).filter((v) => typeof v === 'string');
    if (vals.length) return vals[0];
  }
  return res.statusText || 'Request failed';
}
