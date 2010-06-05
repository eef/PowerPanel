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
        res = "shutdown cancelled"
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
        print "Got connection from %s" % self.client_address[0]
        print self.data
        res = handler.handle(self.data, {"test":"test"})
        self.request.send(res)
        
if __name__ == "__main__":
	HOST = socket.gethostbyname(socket.gethostname())
	PORT = 2501
	
	server = SocketServer.TCPServer((HOST, PORT), AssComms)
	
	server.serve_forever()
