import keystoneMap.*;

Keystone ks;
CornerPinSurface [] surfaces;

PGraphics [] screens;

void setup() {
  size(400,400, P3D);
  smooth();
  
  ks = new Keystone(this);
  screens = new PGraphics[2];
  surfaces = new CornerPinSurface[2];
  
  surfaces[0] = ks.createQuadPinSurface(100, 100, 20);
  surfaces[1] = ks.createTriPinSurface(200, int(sqrt(3)/2*200), 20);
  
  for (int i = 0; i < screens.length; i++) {
    screens[i] = createGraphics(surfaces[i].getWidth(), surfaces[i].getHeight(), P3D);
  }
}

void draw() {
  background(0);
  
  int id = 0;
  for (PGraphics s: screens) {
    s.beginDraw();
    s.background(100+id*50);
    s.fill(255, 0, 0);
    s.textSize(50);
    s.text(id++, s.width/2, 100);
    s.endDraw();
  }
  
  for (int i = 0; i < screens.length; i++) {
  surfaces[i].render(screens[i]);
  }
}

void keyPressed() {
  switch(key) {
  case 'c':
    // enter/leave calibration mode, where surfaces can be warped 
    // and moved
    ks.toggleCalibration();
    break;

  case 'l':
    // loads the saved layout
    ks.load();
    break;

  case 's':
    // saves the layout
    ks.save();
    break;
  }
}