import os
import socket

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

class AssComms():
    def receive(self, options = {}):
        handler = AssHandler()
        HOST = '192.168.0.106'
        PORT = 2501

        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind((HOST, PORT))
        s.listen(1)
        conn, addr = s.accept()

        print "Connection from",addr

        while 1:
            data = conn.recv(1024)
            print "bogla"
            res = handler.handle(data,{'test':'test_options'})
            print data
            conn.send(res)
            conn.close()
            
handler = AssHandler()
comms = AssComms()
comms.receive()
#handler.handle("shutdown", {'test':'test_options'})
