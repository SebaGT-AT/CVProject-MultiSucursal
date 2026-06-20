const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

async function parseResponse(response) {
  const contentType = response.headers.get("content-type") ?? "";
  const payload = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message =
      typeof payload === "object" && payload !== null
        ? payload.error ?? payload.message ?? "Request failed"
        : "Request failed";
    throw new Error(message);
  }

  return payload;
}

export async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers ?? {})
    },
    ...options
  });

  return parseResponse(response);
}

export { API_URL };
