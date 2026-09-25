const statusArea = document.querySelector('#status');
const resultArea = document.querySelector('#result');

async function requestService(path) {
  statusArea.textContent = 'Waiting for the sequential server...';
  resultArea.textContent = '';
  try {
    const response = await fetch(path);
    const body = await response.json();
    if (!response.ok) {
      throw new Error(body.error || 'The request was rejected.');
    }
    statusArea.textContent = 'Success';
    resultArea.textContent = JSON.stringify(body, null, 2);
  } catch (error) {
    statusArea.textContent = error instanceof TypeError ? 'Network error' : 'Request error';
    resultArea.textContent = error.message;
  }
}

document.querySelector('[data-service="greeting"]').addEventListener('submit', (event) => {
  event.preventDefault();
  const name = new FormData(event.currentTarget).get('name').trim();
  if (!name) {
    statusArea.textContent = 'Request error';
    resultArea.textContent = 'Enter a name first.';
    return;
  }
  requestService(`/api/greeting?name=${encodeURIComponent(name)}`);
});

document.querySelector('[data-service="square"]').addEventListener('submit', (event) => {
  event.preventDefault();
  const value = new FormData(event.currentTarget).get('value');
  if (!/^-?\d+$/.test(value)) {
    statusArea.textContent = 'Request error';
    resultArea.textContent = 'Enter a whole number.';
    return;
  }
  requestService(`/api/square?value=${encodeURIComponent(value)}`);
});

document.querySelector('#time').addEventListener('click', () => requestService('/api/time'));
