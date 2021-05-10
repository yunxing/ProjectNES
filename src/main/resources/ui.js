// Copy pasted from https://stackoverflow.com/questions/7812514/drawing-a-dot-on-html5-canvas
var canvas = document.getElementById("screen");
var canvasWidth = canvas.width;
var canvasHeight = canvas.height;
var ctx = canvas.getContext("2d");
ctx.scale(10, 10);
var canvasData = ctx.getImageData(0, 0, canvasWidth, canvasHeight);
// That's how you define the value of a pixel //
function drawPixel (x, y, r, g, b, a) {
    var index = (x + y * canvasWidth) * 4;

    canvasData.data[index + 0] = r;
    canvasData.data[index + 1] = g;
    canvasData.data[index + 2] = b;
    canvasData.data[index + 3] = a;
}

// That's how you update the canvas, so that your //
// modification are taken in consideration //
function updateCanvas() {
    ctx.putImageData(canvasData, 0, 0);
}


let cpu = new ProjectNES.main.CPU()
console.log(cpu.name)

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
async function loadUrl(path) {
  return new Promise((resolve, reject) => {
    var request = new XMLHttpRequest();
    request.open('GET', path, true);
    request.responseType = 'blob';
    request.onload = function() {
      resolve(request.response)
    };
    request.send();
  });
};

async function readFile(path) {
    let blob = await loadUrl(path)
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = res => {
        resolve(res.target.result);
      };
      reader.onerror = err => reject(err);
      reader.readAsArrayBuffer(blob);
    });
  }
async function demo() {
  let rom = await readFile("snake.nes");
  console.log(rom)
  var uint8View = new Uint8Array(rom);
  let cpu = ProjectNES.main.runROM(uint8View);
  let should_continue = true;
  while (should_continue) {
    await sleep(0.5)
    should_continue = cpu.tick()
    for (y = 0; y < 32; y++) {
      for (x = 0; x < 32; x++) {
        let linear_pos = 0x200 + y * 32 + x
        let v = cpu.memRead(linear_pos) * 100
        drawPixel(x, y, v, v, v, 255);
      }
    }

    updateCanvas();
  }
  console.log('done');
}

demo()