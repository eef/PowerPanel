import hashlib
import sys
import os
from wx import wx, Frame, App, Menu, MenuBar, EVT_MENU
from twisted.python import log
from twisted.internet import wxreactor
from twisted.internet.protocol import DatagramProtocol
wxreactor.install()
from twisted.internet import reactor
from hashlib import sha1
import xml.dom.minidom

ID_EXIT  = 101
ID_START  = 102
ID_STOP  = 103
ID_CFG  = 104
ID_ABOUT = 105
ID_EXIT_CONFIG  = 106
ID_SAVE_CONFIG  = 107
ID_SALT  = 108
ID_ICON_TIMER = wx.NewId()

class  Handler(object):

  def get_mac_address(self):
    if sys.platform == 'win32':
      for line in os.popen("ipconfig /all"):
        if line.lstrip().startswith('Physical Address'):
          mac = line.split(':')[1].strip().replace('-', ':')
          return mac
    else:
      for line in os.popen("/sbin/ifconfig"):
        if 'Ether' in line:
          mac = line.split()[4]
      return mac

  def get_pkey(self):
    self.settings = Settings()
    mac = self.get_mac_address()
    salt = self.settings.get_salt()
    hashed = hashlib.sha1(mac + salt)
    return hashed.hexdigest()
  

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
    
  def handle_shutdown(self, options={"delay":"180"}):
    shutdown_string = "shutdown /s /t %s" % options['delay']
    os.system(shutdown_string)
    res = "Computer will shutdown in %s " % self.format_secs(int(options['delay']))
    return res
        
  def handle_cancel(self, options={"delay":"180"}):
    os.system("shutdown /a")
    res = "Shutdown cancelled"
    return res
        
  def handle_reboot(self, options={"delay":"180"}):
    os.system("shutdown /r")
    res = "Rebooting"
    return res
        
  def handle_info(self):
    return "panima"

  def handle_hibernate(self, options={"delay":"180"}):
    os.system("shutdown /h")
    res = "Computer hibernating"
    return res

  def handle_hello(self, options={"delay":"180"}):
    res = self.get_pkey()
    return res
        
  def handle_pair(self, options={"delay":"180"}):
    print "handling pair...";
    pair = "no"
    dlg = wx.MessageDialog(None, "Do you want to pair?", "Confirm Pair", wx.OK|wx.CANCEL|wx.ICON_QUESTION)
    result = dlg.ShowModal()
    dlg.Destroy()
    if result == wx.ID_OK:
      pair = "yes"
    elif result == wx.ID_CANCEL:
      pair = "no"
    res = "{'pairaccepted':'%s', 'pkey':'%s','mac':'%s','name':'%s'}" % (pair, self.get_pkey(), self.get_mac_address(), os.getenv("COMPUTERNAME"))
    return res
        

    
  def handle(self, type, options):
    print "handling something..."
    func = getattr(self, 'handle_%s' % type, None)
    if func is None:

      return False
    return func(options)

class MyProtocol(DatagramProtocol):
  def datagramReceived(self, data, (host, port)):
    print "Packet recived"
    handler = Handler()
    print "Data: " + data
    data_split = data.split(":")
    print data_split
    res = handler.handle(data_split[0], options={'delay':data_split[1]})
    self.transport.write(res, (host, port))


class MyFrame(Frame):
  def __init__(self, parent, ID, title):
    Frame.__init__(self, parent, ID, title, wx.DefaultPosition, size=(180, 160))
    self.settings = Settings()
    self.settings.get_salt()
    self.SetSizeHints(180,160,180,160)
    self.CreateStatusBar()
    self.tbicon = wx.TaskBarIcon()
    self.icon_state = False
    icon = wx.Icon('icon.png', wx.BITMAP_TYPE_PNG)
    self.tbicon.SetIcon(icon, '')
    wx.EVT_TASKBAR_LEFT_UP(self.tbicon, self.OnTaskBarLeftDClick)
    wx.EVT_TASKBAR_RIGHT_UP(self.tbicon, self.OnTaskBarRightClick)
    file_menu = Menu()
    help_menu = Menu()
    file_menu.Append(ID_EXIT, "E&xit", "Exit PowerPanel")
    file_menu.Append(ID_CFG, "C&onfig", "Configure PowerPanel")
    help_menu.Append(ID_ABOUT, "A&bout", "About PowerPanel")
    menuBar = MenuBar()
    menuBar.Append(file_menu, "&File")
    menuBar.Append(help_menu, "&Help")
    self.SetStatusText("Server: Offline")
    self.SetMenuBar(menuBar)
    EVT_MENU(self, ID_EXIT,  self.DoExit)
    EVT_MENU(self, ID_ABOUT,  self.OnAboutBox)
    EVT_MENU(self, ID_CFG,  self.OpenConfig)

    panel = wx.Panel(self, -1)
    vbox = wx.BoxSizer(wx.VERTICAL)

    vbox.Add((-1, 25))

    hbox5 = wx.BoxSizer(wx.HORIZONTAL)
    btn1 = wx.Button(panel, ID_START, 'Start', size=(70, 30))
    hbox5.Add(btn1, 0)
    btn2 = wx.Button(panel, ID_STOP, 'Stop', size=(70, 30))
    hbox5.Add(btn2, 0, wx.LEFT | wx.BOTTOM , 5)
    vbox.Add(hbox5, 0, wx.ALIGN_RIGHT | wx.RIGHT, 10)

    self.Bind(wx.EVT_BUTTON, self.OnStart, id=ID_START)
    self.Bind(wx.EVT_BUTTON, self.OnStop, id=ID_STOP)
    self.Bind(wx.EVT_CLOSE, self.OnClose)

    panel.SetSizer(vbox)
    self.Centre()

  def DoExit(self, event):
    self.tbicon.RemoveIcon()
    reactor.stop()
    exit()
    
  def OnClose(self, event):
    self.tbicon.RemoveIcon()
    reactor.stop()
    exit()
    
  def OpenConfig(self, event):
    cfg_frame = Config(None, -1, "Configuration")
    cfg_frame.Show(True)
    return True
    
  def OnStart(self, event):
    if self.settings.load() == True:
      self.port = reactor.listenUDP(2501, MyProtocol())
      self.SetStatusText("Server: Online")
    else:
      dlg = wx.MessageDialog(None, "No configuration present.", "No config", wx.OK|wx.ICON_INFORMATION)
      dlg.ShowModal()
      dlg.Destroy()
      cfg_frame = Config(None, -1, "Configuration")
      cfg_frame.Show(True)
      
  def OnStop(self, event):
    self.port.stopListening()
    self.SetStatusText("Server: Offline")
    
  def OnAboutBox(self, event):
    description = """Power Panel is a remote shutdown application for Android based phones."""
    info = wx.AboutDialogInfo()
    info.SetName('Power Panel')
    info.SetVersion('1.0')
    info.SetDescription(description)
    info.SetCopyright('(C) 2010 WellBaked')
    info.SetWebSite('http://www.wellbaked.net')
    info.AddDeveloper('Will McGregor / Arthur Canal')
    info.AddDocWriter('Will McGregor / Arthur Canal')
    info.AddArtist('WellBaked')
    info.AddTranslator('Will McGregor / Arthur Canal')
    wx.AboutBox(info)

  def OnTaskBarLeftDClick(self, evt):
        self.Show()

  def ServerIsRunning(self, state):
      if state == True:
          icon = wx.Icon('not_running.pmg', wx.BITMAP_TYPE_PNG)
          self.tbicon.SetIcon(icon, 'Not Running')
      else:
          icon = wx.Icon('running.png', wx.BITMAP_TYPE_PNG)
          self.tbicon.SetIcon(icon, 'Running')

  def OnTaskBarRightClick(self, evt):
      #right clicking icon should pop up the main window
      self.Hide()
      wx.GetApp().ProcessIdle()



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


class Settings():

  def getText(self, nodelist):
    rc = []
    for node in nodelist:
      if node.nodeType == node.TEXT_NODE:
        rc.append(node.data)
    return ''.join(rc)

  def is_setup(self, element):
    if int(element.attributes["saved"].value) == 1:
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
      return True
    else:
      return False

  def get_salt(self):
    dom = self.load_dom()
    parent = dom.getElementsByTagName("config")[0]
    salt_node = parent.getElementsByTagName("salt")[0]
    return self.getText(salt_node.childNodes)
    

  def getText(self, nodelist):
    rc = []
    for node in nodelist:
      if node.nodeType == node.TEXT_NODE:
        rc.append(node.data)
    return ''.join(rc)

  def save_salt(self, salt):
    file = open("config.xml", "w+")
    salted = """<?xml version="1.0" encoding="windows-1252"?>
<config saved="1">
  <salt>%s</salt>
</config>
  """ % salt
    file.write(salted)
    file.close()
  
  
class Config(Frame):
  def __init__(self, parent, ID, title):
    Frame.__init__(self, parent, ID, title, wx.DefaultPosition, size=(390, 150))
    self.CreateStatusBar()
    self.settings = Settings()
    self.SetStatusText("Configuration")
    self.Bind(wx.EVT_CLOSE, self.OnClose)
    self.Bind(wx.EVT_BUTTON, self.OnClose, id=ID_EXIT_CONFIG)
    self.Bind(wx.EVT_BUTTON, self.SaveSalt, id=ID_SAVE_CONFIG)
    self.window = self
    panel = wx.Panel(self, -1)

    vbox = wx.BoxSizer(wx.VERTICAL)

    hbox1 = wx.BoxSizer(wx.HORIZONTAL)
    st1 = wx.StaticText(panel, -1, 'Password')
    hbox1.Add(st1, 0, wx.RIGHT, 8)
    self.tc = wx.TextCtrl(panel, ID_SALT)
    if self.settings.load() == True:
      self.tc.SetValue(self.settings.get_salt())
    hbox1.Add(self.tc, 1)
    vbox.Add(hbox1, 0, wx.EXPAND | wx.LEFT | wx.RIGHT | wx.TOP, 10)

    vbox.Add((-1, 25))

    hbox5 = wx.BoxSizer(wx.HORIZONTAL)
    btn1 = wx.Button(panel, ID_SAVE_CONFIG, 'Save', size=(70, 30))
    hbox5.Add(btn1, 0)
    btn2 = wx.Button(panel, ID_EXIT_CONFIG, 'Close', size=(70, 30))
    hbox5.Add(btn2, 0, wx.LEFT | wx.BOTTOM , 5)
    vbox.Add(hbox5, 0, wx.ALIGN_RIGHT | wx.RIGHT, 10)

    panel.SetSizer(vbox)
    self.Centre()
    
  def OnClose(self, event):
    self.Hide()

  def SaveSalt(self, event):
    self.settings = Settings()
    self.settings.save_salt(self.tc.GetValue())
    self.Hide()


if __name__ == '__main__':
  PowerPanel()
