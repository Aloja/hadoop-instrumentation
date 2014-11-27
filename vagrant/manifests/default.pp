# Set default path for all commands
Exec { path => [ "/bin/", "/sbin/" , "/usr/bin/", "/usr/sbin/" ] }


# Run this before anything else
stage { 'init':
    before => Stage['main'],
}
class { 'prepare': 
    stage => init,
}

class prepare {

    # Prefer ipv4 over ipv6 for faster downloads
    exec { 'prefer_ipv4':
        command => 'echo "\nprecedence ::ffff:0:0/96  100" >> /etc/gai.conf',
        unless => 'grep "^precedence ::ffff:0:0/96  100$" /etc/gai.conf',
    }


    # Configure apt to install less dependencies
    file { 'apt-settings':
        path => '/etc/apt/apt.conf.d/90localsettings',
        source  => '/vagrant/files/apt-90localsettings',
        owner => 'root',
        group => 'root',
    }

}


# Better apt mirrors
class { 'apt':
    purge_sources_list   => true,
    purge_sources_list_d => true,
}
apt::source { 'ubuntu_trusty':
    location          => 'http://ftp.udc.es/ubuntu/',
    release           => 'trusty',
    repos             => 'main restricted universe',
    include_deb       => true,
    include_src       => false,
}
apt::source { 'ubuntu_trusty-updates':
    location          => 'http://ftp.udc.es/ubuntu/',
    release           => 'trusty-updates',
    repos             => 'main restricted universe',
    include_deb       => true,
    include_src       => false,
}
apt::source { 'ubuntu_trusty-security':
    location          => 'http://security.ubuntu.com/ubuntu',
    release           => 'trusty-security',
    repos             => 'main restricted universe',
    include_deb       => true,
    include_src       => false,
}


# Java
apt::ppa { 'ppa:webupd8team/java': }
exec { 'set-licence-selected':
    command => 'echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections',
    before => Package['java'],
    subscribe => Apt::Ppa['ppa:webupd8team/java'],
    refreshonly => true,
}
exec { 'set-licence-seen':
    command => 'echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections',
    before => Package['java'],
    subscribe => Apt::Ppa['ppa:webupd8team/java'],
    refreshonly => true,
}
package { ['java']:
    name => 'oracle-java7-installer',
    ensure => installed,
    require => Apt::Ppa['ppa:webupd8team/java'],
}
package { ['ant']:
    ensure => installed,
    require => Package['java'],
}


# Install various packages
Apt::Source <| |> -> package { ['build-essential', 'dh-autoreconf', 'git', 'less', 'libpcap-dev', 'libxml2-dev', 'screen', 'vim']:
    ensure => installed,
}


# Remove unnecessary packages
package { ['landscape-client', 'landscape-common']:
    ensure => purged,
}


# Link mounted folder to home for easier access
file { '/home/vagrant/workspace':
    ensure => link,
    target => '/vagrant/workspace',
}


# Shell config
file { '/home/vagrant/.bash_aliases':
    source  => '/vagrant/files/bash_aliases',
}


# Configure passwordless SSH to localhost
exec { 'ssh-keygen':
    user => 'vagrant',
    command => 'ssh-keygen -t rsa -P "" -f /home/vagrant/.ssh/id_rsa',
    creates => '/home/vagrant/.ssh/id_rsa',
}
exec { 'authorized_keys':
    user => 'vagrant',
    command => 'cat /home/vagrant/.ssh/id_rsa.pub >> /home/vagrant/.ssh/authorized_keys',
    require => Exec['ssh-keygen'],
    unless => 'grep "`cat /home/vagrant/.ssh/id_rsa.pub`" /home/vagrant/.ssh/authorized_keys',
}
exec { 'known_hosts':
    user => 'vagrant',
    command => 'ssh-keyscan -H `hostname` >> /home/vagrant/.ssh/known_hosts \
                && ssh-keyscan -H `ip -oneline -4 addr show eth0 | grep -Po "inet \K[\d.]+"` >> /home/vagrant/.ssh/known_hosts \
                && ssh-keyscan -H `hostname`,`ip -oneline -4 addr show eth0 | grep -Po "inet \K[\d.]+"` >> /home/vagrant/.ssh/known_hosts \
                && ssh-keyscan -H localhost >> /home/vagrant/.ssh/known_hosts \
                && ssh-keyscan -H 127.0.0.1 >> /home/vagrant/.ssh/known_hosts \
                && ssh-keyscan -H localhost,127.0.0.1 >> /home/vagrant/.ssh/known_hosts',
    require => Exec['ssh-keygen'],
    creates => '/home/vagrant/.ssh/known_hosts',
}