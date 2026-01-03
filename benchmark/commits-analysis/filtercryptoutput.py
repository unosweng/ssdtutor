import re
import fileinput
import csv

isempty = True
bufferlines = ''
projname = ''

iscrypto = {}
with open('git_repo_cryptopackage.csv') as cryptofiles:
        csvreader = csv.reader(cryptofiles, delimiter=',')
        next(csvreader)
        for row in csvreader:
		iscrypto[row[0]] = True


for line in fileinput.input():
	line = line.rstrip('\n')
	if re.match(r'^----------------------', line, re.M):
		if not isempty:
			if projname in iscrypto:
				print bufferlines
		isempty = True
		bufferlines = line
	else:
		if re.match(r'^[a-z0-9]', line, re.M):
			isempty = False
		rex = re.match(r'^\.\./commits/(.*)$', line, re.M)
		if rex:
			projname = rex.group(1)
		bufferlines += '\n' + line



if not isempty:
	if projname in iscrypto:
		print bufferlines
