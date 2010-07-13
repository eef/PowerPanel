import time

import SocketServer
import os
import socket
import wx
import xml.dom.minidom
from threading import *

ID_START = wx.NewId()
ID_STOP = wx.NewId()

class Server():
  def __init__(self):
    HOST = socket.gethostbyname(socket.gethostname())
    PORT = 2501
    server = SocketServer.UDPServer((HOST, PORT), Comms)
    server.serve_forever()

class  Handler(object):
    
  def format_secs(self, seconds):
    hours = seconds / 3600
    seconds -= 3600 * hours
    minutes = seconds / 60
    seconds -= 60 * minutes
    if hours == 0:
      return "%02d mins %02d secs" % (minutes, seconds)
      if minutes == 0:
        return "%d secs" % (seconds)
    return "%02d hours %02d mins %02d secs" % (hours, minutes, seconds)
    
  def handle_shutdown(self, options={"delay":"3000"}):
    shutdown_string = "shutdown /s /t %s" % options['delay']
    os.system(shutdown_string)
    res = "Computer will shutdown in %s " % self.format_secs(int(options['delay']))
    return res
        
  def handle_cancel(self):
    os.system("shutdown /a")
    res = "Shutdown cancelled"
    return res
        
  def handle_reboot(self):
    os.system("shutdown /r")
        
  def handle_info(self):
    return "panima"

  def handle_hibernate(self):
    os.system("shutdown /h")
    res = "Shutdown successful"
    return res

  def handle_hello(self):
    res = "Found computer"
    return res
        
  def handle_pair(self):
    # Mocking some data until I get it in a config
    res = "{ 'pairaccepted' : 'yes', 'pkey':'123456788','mac':'00-1F-D0-5C-3A-BB'}"
    return res
        
  def handle(self, type, * args, ** kwargs):
    func = getattr(self, 'handle_%s' % type, None)
    if func is None:
      return False
    return func(*args, ** kwargs)

class Comms(SocketServer.BaseRequestHandler):

  def handle(self, options={}):
    handler = Handler()
    data = self.request[0].strip()
    socket = self.request[1]
    print "%s wrote %s" % (self.client_address[0], data)
    res = handler.handle(data)
    if res == False:
      res = "Unknown command"
    socket.sendto(res, self.client_address)

class Config():

  def getText(self, nodelist):
    rc = []
    for node in nodelist:
      if node.nodeType == node.TEXT_NODE:
        rc.append(node.data)
    return ''.join(rc)

  def is_setup(self, element):
    if element.attributes["saved"].value == 1:
      return True
    else:
      return False

  def load_dom(self):
    dom = xml.dom.minidom.parse("config.xml")
    return dom
  
  def load(self):
    dom = self.load_dom()
    saved = dom.getElementsByTagName("config")[0]
    if self.is_setup(saved):
      return False
    else:
      print dom.getElementsByTagName("config").childNodes

ID_START = wx.NewId()
ID_STOP = wx.NewId()

EVT_RESULT_ID = wx.NewId()

def EVT_RESULT(win, func):
  win.Connect(-1, -1, EVT_RESULT_ID, func)

class ResultEvent(wx.PyEvent):
  def __init__(self, data):
    wx.PyEvent.__init__(self)
    self.SetEventType(EVT_RESULT_ID)
    self.data = data

class WorkerThread(Thread):
  def __init__(self, notify_window):
    Thread.__init__(self)
    self._notify_window = notify_window
    self._want_abort = 0
    self.start()

  def run(self):
    HOST = socket.gethostbyname(socket.gethostname())
    PORT = 2501
    self.server = SocketServer.UDPServer((HOST, PORT), Comms)
    self.server.serve_forever()
    while(self._want_abort == 0):
      time.sleep(1)
      if self._want_abort:
        wx.PostEvent(self._notify_window, ResultEvent(None))
        return
    wx.PostEvent(self._notify_window, ResultEvent(10))

  def abort(self):
    self.server.shutdown()
    self._want_abort = 1

class MainFrame(wx.Frame):
  def __init__(self, parent, id):
    wx.Frame.__init__(self, parent, id, 'Shutdown')

    wx.Button(self, ID_START, 'Start', pos=(0, 0))
    wx.Button(self, ID_STOP, 'Stop', pos=(0, 50))
    self.status = wx.StaticText(self, -1, '', pos=(0, 100))

    self.Bind(wx.EVT_BUTTON, self.OnStart, id=ID_START)
    self.Bind(wx.EVT_BUTTON, self.OnStop, id=ID_STOP)

    EVT_RESULT(self, self.OnResult)

    self.worker = None

  def OnStart(self, event):
    if not self.worker:
      self.status.SetLabel('Starting server')
      self.worker = WorkerThread(self)

  def OnStop(self, event):
    if self.worker:
      self.status.SetLabel('Shutting server down...')
      self.worker.abort()

  def OnResult(self, event):
    if event.data is None:
      self.status.SetLabel('Server shutdown complete')
    else:
      self.status.SetLabel('Shizzle: %s' % event.data)
    self.worker = None

class MainApp(wx.App):
  def OnInit(self):
    self.frame = MainFrame(None, -1)
    self.frame.Show(True)
    self.SetTopWindow(self.frame)
    return True

if __name__ == '__main__':
  app = MainApp(0)
  app.MainLoop()
  
