const path = require("path");
const { app, BrowserWindow } = require("electron");

// Use async IIFE to handle dynamic import
(async () => {
  const isDev = (await import("electron-is-dev")).default;

  function createWindow() {
    // Create the browser window.
    const win = new BrowserWindow({
      width: 1200,
      height: 800,
      webPreferences: {
        nodeIntegration: true,
        contextIsolation: false,
      },
    });

    // Load the app
    if (isDev) {
      win.loadURL("http://localhost:3000");
      // Open the DevTools.
      win.webContents.openDevTools();
    } else {
      win.loadFile(path.join(__dirname, "../build/index.html"));
    }
  }

  app.whenReady().then(createWindow);

  app.on("window-all-closed", () => {
    if (process.platform !== "darwin") {
      app.quit();
    }
  });

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
})();
