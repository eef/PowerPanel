import wx
import os
import socket
import SocketServer
    

class  AssHandler(object):
    
    def format_secs(self, seconds):
        hours = seconds / 3600
        seconds -= 3600*hours
        minutes = seconds / 60
        seconds -= 60*minutes
        if hours == 0:
            return "%02d mins %02d secs" % (minutes, seconds)
            if minutes == 0:
                return "%d secs" % (seconds)
        return "%02d hours %02d mins %02d secs" % (hours, minutes, seconds)
    
    def handle_shutdown(self, options = {"delay":"3000"}):
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

    def handle_hello(self):       
        res = "Found computer " + str(HOST) +":"+ str(PORT)
        print "sending: " + res + " to: " + str(HOST) +":"+ str(PORT)
        return res
        
    def handle_pair(self):
        print "pair request recev" + str(HOST) +":"+ str(PORT)
        res = "{ 'pairaccepted' : 'yes', 'pkey':'123456788','mac':'00-1F-D0-5C-3A-BB'}"
        return res
        
    def handle(self, type, *args, **kwargs):       
        func = getattr(self, 'handle_%s' % type, None)
        if func is None:
            return False
        return func(*args, **kwargs)

class AssComms(SocketServer.BaseRequestHandler):
    def handle(self, options = {}):
        handler = AssHandler()
        data = self.request[0].strip()
        socket = self.request[1]
        print "%s wrote:" % self.client_address[0]
        print data
        res = handler.handle(data)
        if res == False:
            res = "Unknown command"
        socket.sendto(res, self.client_address)
        
if __name__ == "__main__":

    def OnTaskBarRight(event):
        app.ExitMainLoop()
    
    HOST = socket.gethostbyname(socket.gethostname())
    PORT = 2501
    print "-----------------------------------"
    print "| Welcome to A.S.S v0.1 alpha (!) |"
    print "-----------------------------------\n\n"
    print " %s listenting on port %s\n\n" % (str(HOST), str(PORT))
    print " Press ctrl-c to exit"
    server = SocketServer.UDPServer((HOST, PORT), AssComms)
    
    server.serve_forever()

    app = wx.PySimpleApp()

    icon = wx.Icon("favicon.ico", wx.BITMAP_TYPE_ICO)

    tbicon = wx.TaskBarIcon()
    tbicon.SetIcon(icon, "I am an Icon")

    wx.EVT_TASKBAR_RIGHT_UP(tbicon, OnTaskBarRight)

    app.MainLoop()

