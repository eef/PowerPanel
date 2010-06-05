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

    def handle_hibernate(self):
        os.system("shutdown /h")

    def handle(self, type, *args, **kwargs):
        func = getattr(self, 'handle_%s' % type, None)
        if func is None:
            raise Exception("Cannot find handler %r" % type)
        return func(*args, **kwargs)

class AssComms(SocketServer.BaseRequestHandler):
    def handle(self, options = {}):
        handler = AssHandler()
        
        self.data = self.request.recv(1024).strip()
        print "Connection from %s" % self.client_address[0]
        print self.data
        res = handler.handle(self.data)
        self.request.send(res)
        
if __name__ == "__main__":
	HOST = socket.gethostbyname(socket.gethostname())
	PORT = 2501
	print "-----------------------------------"
	print "| Welcome to A.S.S v0.1 alpha (!) |"
	print "-----------------------------------\n\n"
	print " %s listenting on port %s\n\n" % (str(HOST), str(PORT))
	print " Press ctrl-c to exit"
	server = SocketServer.TCPServer((HOST, PORT), AssComms)
	server.serve_forever()

