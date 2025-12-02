const { Server } = require("socket.io");

const io = new Server(9999, {
  cors: {
    origin: "*", // Allow connections from anywhere (Android, Electron, localhost)
  }
});

console.log("Starting Socket.IO server on port 9999...");

io.on("connection", (socket) => {
  console.log(`Client connected: ${socket.id}`);

  // Listen for 'new-user' events from clients
  socket.on("new-user", (user) => {
    console.log("Received 'new-user' event:", user);
    
    // Broadcast the new user to ALL connected clients (including the sender)
    io.emit("new-user", user);
  });

  socket.on("disconnect", () => {
    console.log(`Client disconnected: ${socket.id}`);
  });
});
