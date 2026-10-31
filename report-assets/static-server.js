const http = require("http");
const fs = require("fs");
const path = require("path");

const root = process.cwd();
const port = 8765;

const contentTypes = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".svg": "image/svg+xml",
  ".ico": "image/x-icon",
  ".json": "application/json; charset=utf-8"
};

function send(res, status, body, type) {
  res.writeHead(status, { "Content-Type": type });
  res.end(body);
}

http.createServer((req, res) => {
  const requestPath = req.url === "/" ? "/report-assets/db-table-screenshots.html" : req.url;
  const localPath = path.normalize(path.join(root, decodeURIComponent(requestPath)));

  if (!localPath.startsWith(root)) {
    send(res, 403, "Forbidden", "text/plain; charset=utf-8");
    return;
  }

  fs.readFile(localPath, (error, data) => {
    if (error) {
      send(res, 404, "Not Found", "text/plain; charset=utf-8");
      return;
    }

    const ext = path.extname(localPath).toLowerCase();
    send(res, 200, data, contentTypes[ext] || "application/octet-stream");
  });
}).listen(port, "127.0.0.1", () => {
  console.log(`Static server running at http://127.0.0.1:${port}`);
});
