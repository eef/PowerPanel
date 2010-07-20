import sys
import os
from wx import wx, Frame, App, Menu, MenuBar, EVT_MENU, EVT_BUTTON
from twisted.python import log
from twisted.internet import wxreactor
from twisted.internet.protocol import DatagramProtocol
wxreactor.install()
from twisted.internet import reactor

ID_EXIT  = 101
ID_START  = 102
ID_STOP  = 103
ID_CFG  = 104
ID_ABOUT = 105

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
    print "Shutting down..."
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
    res = "{ 'pairaccepted' : 'yes', 'pkey':'123456788','mac':'asdfghjkl'}"
    return res
        

    
  def handle(self, type, * args, ** kwargs):
    func = getattr(self, 'handle_%s' % type, None)
    if func is None:
      return False
    return func(*args, ** kwargs)

class MyProtocol(DatagramProtocol):
  def datagramReceived(self, data, (host, port)):
    handler = Handler()
    print "received %r from %s:%d" % (data, host, port)
    res = handler.handle(data)
    self.transport.write(res, (host, port))


class MyFrame(Frame):
  def __init__(self, parent, ID, title):
    Frame.__init__(self, parent, ID, title, wx.DefaultPosition, size=(200, 160))
    self.SetSizeHints(200,160,200,160)
    self.CreateStatusBar()
    file_menu = Menu()
    help_menu = Menu()
    file_menu.Append(ID_EXIT, "E&xit", "Exit Power Panel")
    file_menu.Append(ID_CFG, "C&onfig", "Configure Power Panel")
    help_menu.Append(ID_ABOUT, "A&bout", "About Power Panel")
    self.SetStatusText("Server: Offline")
    menuBar = MenuBar()
    menuBar.Append(file_menu, "&File")
    menuBar.Append(help_menu, "&Help")
    self.SetMenuBar(menuBar)
    EVT_MENU(self, ID_EXIT,  self.DoExit)
    EVT_MENU(self, ID_ABOUT,  self.OnAboutBox)
    EVT_MENU(self, ID_CFG,  self.OpenConfig)
    wx.Button(self, ID_START, 'Start', pos=(10, 10))
    wx.Button(self, ID_STOP, 'Stop', pos=(100, 10))
    self.status = wx.StaticText(self, -1, '', pos=(10, 50))
    self.Bind(wx.EVT_BUTTON, self.OnStart, id=ID_START)
    self.Bind(wx.EVT_BUTTON, self.OnStop, id=ID_STOP)
    self.Bind(wx.EVT_CLOSE, self.OnClose)

  def DoExit(self, event):
    reactor.stop()
    exit()
    
  def OnClose(self, event):
    reactor.stop()
    exit()
    
  def OpenConfig(self, event):
    cfg_frame = Config(None, -1, "Configuration")
    cfg_frame.Show(True)
    return True
    
  def OnStart(self, event):
    self.port = reactor.listenUDP(2501, MyProtocol())
    self.SetStatusText("Server: Online")
      
  def OnStop(self, event):
    self.port.stopListening()
    self.SetStatusText("Server: Offline")
    
  def OnAboutBox(self, event):
    description = """Power Panel is a remote shutdown application for Android based phones."""
    licence = """Fuck yer license."""
    info = wx.AboutDialogInfo()
    info.SetName('Power Panel')
    info.SetVersion('1.0')
    info.SetDescription(description)
    info.SetCopyright('(C) 2010 WellBaked')
    info.SetWebSite('http://www.well-baked.net')
    info.SetLicence(licence)
    info.AddDeveloper('Billy McGregor & Arthur Canal')
    info.AddDocWriter('Billy McGregor & Arthur Canal')
    info.AddArtist('WellBaked')
    info.AddTranslator('Billy McGregor & Arthur Canal')
    wx.AboutBox(info)



class MyApp(App):
  def OnInit(self):
    frame = MyFrame(None, -1, "Power Panel")
    frame.Show(True)
    self.SetTopWindow(frame)
    return True


def PowerPanel():
  log.startLogging(sys.stdout)
  app = MyApp(0)
  reactor.registerWxApp(app)
  reactor.run()
  
  
class Config(Frame):
  def __init__(self, parent, ID, title):
    Frame.__init__(self, parent, ID, title, wx.DefaultPosition, size=(200, 100))
    self.SetSizeHints(200,100,200,100)
    self.CreateStatusBar()
    self.SetStatusText("Configuration")
    self.Bind(wx.EVT_CLOSE, self.OnClose)
    
  def OnClose(self, event):
    self.Hide()


if __name__ == '__main__':
  PowerPanel()
