
import fileinput
import re

inbody = False
inreason = False
reason = ''

commitdate = {}
commitauth = {}
commitfiles = {}
commitreason = {}

for line in fileinput.input():
	line = line.rstrip('\n')
	if re.match(r'^commit', line, re.M):
		inbody = False
		rex = re.match(r'^commit ([0-9a-z]*)$', line, re.M)
		cid = rex.group(1)
		commitfiles[cid] = []
	elif re.match(r'^Author', line, re.M):
		rex = re.match(r'^Author: (.*) (\<.*\>)$', line, re.M)
		authname = rex.group(1)
		authemail = rex.group(2)
		commitauth[cid] = authemail
	elif re.match(r'^Date', line, re.M):
		rex = re.match(r'^Date: (.*)$', line, re.M)
		cdate = rex.group(1)
		commitdate[cid] = cdate
		inbody = True
		inreason = True
		commitreason[cid] = ''
	elif inbody:
		#rex = re.match(r'^ (.*) \|  *([0-9][0-9]*) ([\+\-]*)', line, re.M)
		rex = re.match(r'^ (.*) \| (.*)$', line, re.M)
		if rex:
			inreason = False
			fname = rex.group(1).lstrip(' ').rstrip(' ')
			commitfiles[cid].append(fname)
			size = rex.group(2)
			rex = re.match(r'  *([0-9][0-9]*) ([\+\-]*)', line, re.M)
			if rex:
				changedlines = rex.group(1)
				plusminus = rex.group(2)
			#print fname, changedlines, plusminus
		elif re.match(r'^ [0-9][0-9]* files changed, [0-9][0-9]* insertions\(\+\), [0-9][0-9]* deletions\(\-\)', line, re.M):
			rex = re.match(r'^ ([0-9][0-9]*) files changed, ([0-9][0-9]*) insertions\(\+\), ([0-9][0-9]*) deletions\(\-\)', line, re.M)
			numchanged = rex.group(1)
			numinserts = rex.group(2)
			numdeletes = rex.group(3)
			#print numchanged, numinserts, numdeletes
			inbody = False
		else:
			commitreason[cid] = commitreason[cid] + line.lstrip(' ')



for cid in commitreason:
	reason = commitreason[cid]
	if 'crypt' in reason.lower():
		print cid, 'R:', reason
		for fname in commitfiles[cid]:
			if fname.endswith('java'):
				print '   ', fname
	else:
		for fname in commitfiles[cid]:
			if 'crypt' in fname.lower():
				print cid, 'F:', reason
				if fname.endswith('java'):
					print '   ', fname
				break
		
		

	#rex = re.match(r'(crypto|encrypt|crypt)', reason.lower(), re.M)
	#if rex:
		#print rex.group(1)
		#print reason.lower()
	
	
