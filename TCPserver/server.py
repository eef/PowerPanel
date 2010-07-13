import SocketServer
import os
import socket
import wx
import xml.dom.minidom

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

if __name__ == "__main__":

  server = Server()
  
