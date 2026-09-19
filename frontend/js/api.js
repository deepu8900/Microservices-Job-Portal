const API_BASE = "http://localhost:8080/api";

function getToken() {
    return localStorage.getItem("jobportal_token");
}

function getUser() {
    const raw = localStorage.getItem("jobportal_user");
    return raw ? JSON.parse(raw) : null;
}

function setSession(authResponse) {
    localStorage.setItem("jobportal_token", authResponse.token);
    localStorage.setItem("jobportal_user", JSON.stringify({
        userId: authResponse.userId,
        fullName: authResponse.fullName,
        email: authResponse.email,
        role: authResponse.role
    }));
}

function clearSession() {
    localStorage.removeItem("jobportal_token");
    localStorage.removeItem("jobportal_user");
}

function requireAuth(expectedRole) {
    const user = getUser();
    if (!user || !getToken()) {
        window.location.href = "index.html";
        return null;
    }
    if (expectedRole && user.role !== expectedRole) {
        window.location.href = "index.html";
        return null;
    }
    return user;
}

async function apiRequest(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    const token = getToken();
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers
    });

    if (!response.ok) {
        let message = `Request failed with status ${response.status}`;
        try {
            const body = await response.json();
            if (body.error) message = body.error;
        } catch (err) {}
        throw new Error(message);
    }

    if (response.status === 204) return null;
    return response.json();
}

function logout() {
    clearSession();
    window.location.href = "index.html";
}
