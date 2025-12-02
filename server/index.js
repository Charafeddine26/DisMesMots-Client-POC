const { Server } = require("socket.io");

const io = new Server(9999, {
  cors: {
    origin: "*",
  }
});

console.log("Starting Socket.IO server on port 9999...");

io.on("connection", (socket) => {
  const clientType = socket.handshake.query.type || "Unknown";
  console.log(`Client connected: ${socket.id} (${clientType})`);


  socket.on("new-user", (user) => {
    console.log("Received 'new-user' event:", user);
    

    io.emit("new-user", user);
  });

  socket.on("disconnect", () => {
    console.log(`Client disconnected: ${socket.id}`);
  });
});
