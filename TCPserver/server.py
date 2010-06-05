import os
import socket
import SocketServer

class  AssHandler(object):
    def handle_shutdown(self, options):
        os.system("shutdown /s /t 6000")
        res = "Computer will shutdown in 1 min"
        return res
        
    def handle_cancel(self, options):
        os.system("shutdown /a")
        res = "Shutdown cancelled"
        return res
        
    def handle_reboot(self, options):
        os.system("shutdown /r")

    def handle_hibernate(self, options):
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
        res = handler.handle(self.data, {"test":"test"})
        self.request.send(res)
        
if __name__ == "__main__":
	hostname = socket.gethostbyname(socket.gethostname())
	HOST = hostname
	PORT = 2501
	print "-----------------------------------"
	print "| Welcome to A.S.S v0.1 alpha (!) |"
	print "-----------------------------------\n\n"
	print " %s listenting on port %s\n\n" % (str(hostname), str(PORT))
	print " Press ctrl-c to exit"
	server = SocketServer.TCPServer((HOST, PORT), AssComms)
	server.serve_forever()

